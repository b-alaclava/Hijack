/*
 * Copyright (c) 2018, Daniel Teo <https://github.com/takuyakanbr>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package ca.arnah.runelite.plugin.config;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import ca.arnah.runelite.plugin.ArnahPluginManifest;
import lombok.Getter;
import net.runelite.client.plugins.config.SearchablePlugin;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.LinkBrowser;
import net.runelite.client.util.SwingUtil;

class ArnahPluginListItem extends JPanel implements SearchablePlugin{
	
	private static final ImageIcon CONFIG_ICON;
	private static final ImageIcon CONFIG_ICON_HOVER;
	private static final ImageIcon ON_STAR;
	private static final ImageIcon OFF_STAR;



	private final ArnahPluginListPanel pluginListPanel;
	
	@Getter
	private final ArnahPluginConfigurationDescriptor pluginConfig;
	
	@Getter
	private final List<String> keywords = new ArrayList<>();
	
	private final JToggleButton pinButton;
	private final JToggleButton onOffToggle;
	
	static{
		Class<?> memes = null;
		try{
			memes = Class.forName("net.runelite.client.plugins.config.ConfigPanel");
		}catch(ClassNotFoundException ex){
			ex.printStackTrace();
		}
		BufferedImage configIcon = ImageUtil.loadImageResource(memes, "config_edit_icon.png");
		BufferedImage onStar = ImageUtil.loadImageResource(memes, "star_on.png");
		CONFIG_ICON = new ImageIcon(configIcon);
		ON_STAR = new ImageIcon(onStar);
		CONFIG_ICON_HOVER = new ImageIcon(ImageUtil.luminanceOffset(configIcon, -100));
		
		BufferedImage offStar = ImageUtil.luminanceScale(ImageUtil.grayscaleImage(onStar), 0.77f);
		OFF_STAR = new ImageIcon(offStar);

	}

	ArnahPluginListItem(ArnahPluginListPanel pluginListPanel, ArnahPluginConfigurationDescriptor pluginConfig){
		this.pluginListPanel = pluginListPanel;
		this.pluginConfig = pluginConfig;

		Collections.addAll(keywords, pluginConfig.getName().toLowerCase().split(" "));
		Collections.addAll(keywords, pluginConfig.getDescription().toLowerCase().split(" "));
		Collections.addAll(keywords, pluginConfig.getTags());
		ArnahPluginManifest mf = pluginConfig.getExternalPluginManifest();
		if(mf != null){
			keywords.add("pluginhub");
			keywords.add(mf.getInternalName());
		}else{
			keywords.add("plugin"); // we don't want searching plugin to only show hub plugins
		}

		setLayout(new BorderLayout(3, 0));
		setPreferredSize(new Dimension(PluginPanel.PANEL_WIDTH, 20));

		JLabel nameLabel = new JLabel(pluginConfig.getName());


		nameLabel.setForeground(Color.WHITE);

		if(!pluginConfig.getDescription().isEmpty()){
			nameLabel.setToolTipText("<html>" + pluginConfig.getName() + ":<br>" + pluginConfig.getDescription() + "</html>");
		}

		pinButton = new JToggleButton(OFF_STAR);
		pinButton.setSelectedIcon(ON_STAR);
		SwingUtil.removeButtonDecorations(pinButton);
		SwingUtil.addModalTooltip(pinButton, "Unpin plugin", "Pin plugin");
		pinButton.setPreferredSize(new Dimension(21, 0));
		add(pinButton, BorderLayout.LINE_START);

		pinButton.addActionListener(e->{
			pluginListPanel.savePinnedPlugins();
			pluginListPanel.refresh();
		});

		final JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(1, 2));
		add(buttonPanel, BorderLayout.LINE_END);




		JMenuItem configMenuItem = null;
		if(pluginConfig.hasConfigurables()){
			JButton configButton = new JButton(CONFIG_ICON);
			configButton.setRolloverIcon(CONFIG_ICON_HOVER);
			SwingUtil.removeButtonDecorations(configButton);
			configButton.setPreferredSize(new Dimension(25, 0));
			configButton.setVisible(false);
			buttonPanel.add(configButton);

			configButton.addActionListener(e->{
				configButton.setIcon(CONFIG_ICON);
				openGroupConfigPanel();
			});

			configButton.setVisible(true);
			configButton.setToolTipText("Edit plugin configuration");

			configMenuItem = new JMenuItem("Configure");
			configMenuItem.addActionListener(e->openGroupConfigPanel());
		}




		JMenuItem uninstallItem = null;
		if(mf != null){
			uninstallItem = new JMenuItem("Uninstall");
			uninstallItem.addActionListener(ev->pluginListPanel.getExternalPluginManager().remove(mf.getInternalName()));
		}



		addLabelPopupMenu(nameLabel,configMenuItem, pluginConfig.createSupportMenuItem(), uninstallItem);
		add(nameLabel, BorderLayout.CENTER);

		onOffToggle = new ArnahPluginToggleButton();
		buttonPanel.add(onOffToggle);
		if(pluginConfig.getPlugin() != null){
			onOffToggle.addItemListener(i->{
				if(onOffToggle.isSelected()){
					pluginListPanel.startPlugin(pluginConfig.getPlugin());
				}else{
					pluginListPanel.stopPlugin(pluginConfig.getPlugin());
				}
			});
		}else{
			onOffToggle.setVisible(false);
		}
	}

	@Override
	public String getSearchableName(){
		return pluginConfig.getName();
	}
	
	@Override
	public boolean isPinned(){
		return pinButton.isSelected();
	}
	
	void setPinned(boolean pinned){
		pinButton.setSelected(pinned);
	}
	
	void setPluginEnabled(boolean enabled){
		onOffToggle.setSelected(enabled);
	}
	
	private void openGroupConfigPanel(){
		pluginListPanel.openConfigurationPanel(pluginConfig);
	}
	
	/**
	 * Adds a mouseover effect to change the text of the passed label to {@link ColorScheme#BRAND_ORANGE} color, and
	 * adds the passed menu items to a popup menu shown when the label is clicked.
	 *
	 * @param label     The label to attach the mouseover and click effects to
	 * @param menuItems The menu items to be shown when the label is clicked
	 */
	static void addLabelPopupMenu(JLabel label, JMenuItem... menuItems){
		final JPopupMenu menu = new JPopupMenu();
		final Color labelForeground = label.getForeground();
		menu.setBorder(new EmptyBorder(5, 5, 5, 5));
		
		for(final JMenuItem menuItem : menuItems){
			if(menuItem == null){
				continue;
			}
			
			// Some machines register mouseEntered through a popup menu, and do not register mouseExited when a popup
			// menu item is clicked, so reset the label's color when we click one of these options.
			menuItem.addActionListener(e->label.setForeground(labelForeground));
			menu.add(menuItem);
		}
		
		label.addMouseListener(new MouseAdapter(){
			private Color lastForeground;
			
			@Override
			public void mouseClicked(MouseEvent mouseEvent){
				Component source = (Component) mouseEvent.getSource();
				Point location = MouseInfo.getPointerInfo().getLocation();
				SwingUtilities.convertPointFromScreen(location, source);
				menu.show(source, location.x, location.y);
			}
			
			@Override
			public void mouseEntered(MouseEvent mouseEvent){
				lastForeground = label.getForeground();
				label.setForeground(ColorScheme.BRAND_ORANGE);
			}
			
			@Override
			public void mouseExited(MouseEvent mouseEvent){
				label.setForeground(lastForeground);
			}
		});
	}
}