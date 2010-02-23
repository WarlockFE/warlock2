/**
 * Warlock, the open-source cross-platform game client
 *  
 * Copyright 2008, Warlock LLC, and individual contributors as indicated
 * by the @authors tag. 
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
/*
 * Created on Sep 17, 2004
 */
package cc.warlock.rcp.views;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Caret;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.PageBook;

import cc.warlock.core.client.IWarlockClient;
import cc.warlock.core.client.IWarlockClientListener;
import cc.warlock.core.client.IWarlockClientViewer;
import cc.warlock.core.client.IWarlockStyle;
import cc.warlock.core.client.PropertyListener;
import cc.warlock.core.client.WarlockClientRegistry;
import cc.warlock.core.client.settings.WarlockWindowProvider;
import cc.warlock.rcp.ui.StreamText;
import cc.warlock.rcp.ui.WarlockEntry;
import cc.warlock.rcp.ui.WarlockPopupAction;
import cc.warlock.rcp.ui.client.SWTStreamListener;
import cc.warlock.rcp.ui.client.SWTWarlockClientListener;
import cc.warlock.rcp.ui.client.SWTWarlockClientViewer;
import cc.warlock.rcp.util.ColorUtil;
import cc.warlock.rcp.util.SoundPlayer;

/**
 * @author marshall
 */
public abstract class GameView extends WarlockView implements IWarlockClientViewer, IWarlockClientListener {
	public static final String VIEW_ID = "cc.warlock.rcp.ui.views.GameView";
	
	protected static GameView firstInstance;
	protected static boolean firstInstanceIsUsed = false;
	protected static ArrayList<GameView> openViews = new ArrayList<GameView>();
	protected static ArrayList<IGameViewFocusListener> focusListeners = new ArrayList<IGameViewFocusListener>();
	protected static GameView gameInFocus;
	
	protected PageBook popupPageBook;
	protected Label emptyPopup;
	protected StreamText streamText;
	protected WarlockEntry entry;
	protected SWTWarlockClientViewer wrapper;
	protected Composite entryComposite;
	protected IWarlockClient client;
	protected Composite mainComposite;
	
	public GameView () {
		super();
		
		if (firstInstance == null) {
			firstInstance = this;
			gameInFocus = this;
		}
		
		// currentCommand = "";
		openViews.add(this);
		wrapper = new SWTWarlockClientViewer(this);
		
		WarlockClientRegistry.addWarlockClientListener(new SWTWarlockClientListener(this));
	}
	
	public static void addGameViewFocusListener (IGameViewFocusListener listener)
	{
		focusListeners.add(listener);
	}
	
	public static void removeGameViewFocusListener (IGameViewFocusListener listener)
	{
		focusListeners.remove(listener);
	}
	
	public static List<GameView> getOpenGameViews ()
	{
		return openViews;
	}
	
	public static GameView getGameViewInFocus ()
	{
		return gameInFocus;
	}
	
	public static void initializeGameView (GameView gameView)
	{
		gameInFocus = gameView;
		
		if (ConnectionView.closeAfterConnect)
		{
			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			IViewPart part = page.findView(ConnectionView.VIEW_ID);
			if (part != null)
				page.hideView(part);
		}
	}
	
	public static GameView createNext (String viewId, String secondId) {
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		try {
			IViewPart part = page.showView(viewId, secondId, IWorkbenchPage.VIEW_ACTIVATE);
			// if there's an error in creating the view, we want to know about it.. don't cast unless we know it's not an errorviewpart
			if (part instanceof GameView)
			{
				GameView nextInstance = (GameView) part;
				initializeGameView(nextInstance);
				return nextInstance;
			}
			
		} catch (PartInitException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static GameView getGameViewForClient(IWarlockClient client) {
		for (GameView view: openViews) {
			if (view.client == client) {
				return view;
			}
		}
		// TODO: Make a GameView and return it. (Null is likely to cause problems in the long run)
		return null;
	}
	
	public void createPartControl(Composite parent) {
		// Create main composite
		mainComposite = new Composite (parent, SWT.NONE);
		GridLayout mainLayout = new GridLayout(1, false);
		mainLayout.marginHeight = 0;
		mainLayout.marginWidth = 0;
		mainLayout.horizontalSpacing = 0;
		mainLayout.verticalSpacing = 0;
		mainComposite.setLayout(mainLayout);
		
		// create a pagebook for the popups
		popupPageBook = new PageBook(mainComposite, SWT.NONE);
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, false);
		data.exclude = true;
		popupPageBook.setLayoutData(data);
		
		emptyPopup = new Label(popupPageBook, SWT.NONE);
		
		popupPageBook.showPage(emptyPopup);
		popupPageBook.setVisible(false);
		
		streamText = new StreamText(mainComposite, IWarlockClient.DEFAULT_STREAM_NAME);
		// FIXME: update this when it changes
		streamText.setLineLimit(client.getClientPreferences().getNode().getInt("buffer-lines", 5000));
		streamText.getTextWidget().setLayout(new GridLayout(1, false));
		
		// create the entry
		entryComposite = new Composite(mainComposite, SWT.NONE);
		GridLayout entryLayout = new GridLayout(1, false);
		entryLayout.horizontalSpacing = 0;
		entryLayout.verticalSpacing = 0;
		entryLayout.marginHeight = 1;
		entryLayout.marginWidth = 0;
		entryComposite.setLayout(entryLayout);
		entryComposite.setLayoutData(new GridData(GridData.FILL, GridData.VERTICAL_ALIGN_END, true, false));
		
		this.entry = new WarlockEntry(entryComposite, wrapper); // Do this BEFORE getTextForClient!
		
		initColors();
	}
	
	
	protected void initColors()
	{
		IWarlockStyle style = WarlockWindowProvider.getInstance().get(client.getClientPreferences(), "main");
		Color background = ColorUtil.warlockColorToColor(style.getBackgroundColor());
		Color foreground = ColorUtil.warlockColorToColor(style.getForegroundColor());
		
		entry.getWidget().setBackground(background);
		entry.getWidget().setForeground(foreground);
		
		//streamText.setBackgroundMode(SWT.INHERIT_DEFAULT);
	}
	
	@Override
	public void setFocus() {
		super.setFocus();
		gameInFocus = this;
		for (IGameViewFocusListener listener : focusListeners)
		{
			listener.gameViewFocused(this);
		}
	}
	
	public void playSound(InputStream soundStream) {
		SoundPlayer.play(soundStream);
	}
	
	protected Image createCaretImage (int width, Color foreground)
	{
		PaletteData caretPalette = new PaletteData(new RGB[] {
				new RGB(0, 0, 0), new RGB(255, 255, 255) });
		
		int widthOffset = width - 1;
		ImageData imageData = new ImageData(4 + widthOffset,
				entry.getWidget().getLineHeight(), 1, caretPalette);
		Display display = entry.getWidget().getDisplay();
		Image bracketImage = new Image(display, imageData);
		GC gc = new GC(bracketImage);
		gc.setForeground(foreground);
		gc.setLineWidth(1);
		// gap between two bars of one third of the height
		// draw boxes using lines as drawing a line of a certain width produces
		// rounded corners.
		for (int i = 0; i < width; i++) {
			gc.drawLine(i, 0, i, imageData.height - 1);
		}

		gc.dispose();

		return bracketImage;
	}

	protected Caret createCaret (int width, Color foreground) {
		Caret caret = new Caret(entry.getWidget(), SWT.NULL);
		Image image = createCaretImage(width, foreground);
		
		if (image != null)
			caret.setImage(image);
		else
			caret.setSize(width, entry.getWidget().getLineHeight());

		caret.setFont(entry.getWidget().getFont());

		return caret;
	}
	
	public String getCurrentCommand ()
	{
		return entry.getText();
	}
	
	public void setCurrentCommand(String command) {
		if(command == null) {
			command = "";
		}
		GameView.this.entry.setText(command);
		GameView.this.entry.setSelection(command.length());
	}
	
	public void append(char c) {
		entry.append(c);
	}
	
	public void nextCommand() {
		entry.nextCommand();
	}
	
	public void prevCommand() {
		entry.prevCommand();
	}
	
	public void searchHistory() {
		entry.searchHistory();
	}
	
	public void submit() {
		entry.submit();
	}
	
	public void repeatLastCommand() {
		entry.repeatLastCommand();
	}
	
	public void repeatSecondToLastCommand() {
		entry.repeatSecondToLastCommand();
	}
	
	public void copy() {
		streamText.copy();
	}
	
	public void paste() {
		entry.getWidget().paste();
	}
	
	public IWarlockClient getWarlockClient() {
		return client;
	}
	
	public WarlockEntry getWarlockEntry() {
		return entry;
	}
	
	public WarlockPopupAction createPopup ()
	{
		WarlockPopupAction popup = new WarlockPopupAction(popupPageBook, SWT.NONE);
//		popup.moveAbove(book);
//		popup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
//		popup.setVisible(false);
		
		return popup;
	}
	
	public void showPopup (WarlockPopupAction popup)
	{
		boolean atBottom = streamText.isAtBottom();
		popupPageBook.showPage(popup);
		popupPageBook.setVisible(true);
		((GridData)popupPageBook.getLayoutData()).exclude = false;
		
		mainComposite.layout();
		streamText.postTextChange(atBottom);
	}
	
	public void hidePopup (WarlockPopupAction popup)
	{
		boolean atBottom = streamText.isAtBottom();
		popupPageBook.showPage(emptyPopup);		
		popupPageBook.setVisible(false);
		((GridData)popupPageBook.getLayoutData()).exclude = true;
		
		mainComposite.layout();
		streamText.postTextChange(atBottom);
	}
	
	@Override
	public void dispose() {
		if (client != null && client.getConnection() != null && client.getConnection().isConnected())
		{
			try {
				client.getConnection().disconnect();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		openViews.remove(this);
		if (gameInFocus == this) {
			gameInFocus = null;
		}
		if (firstInstance == this) {
			if (openViews.isEmpty()) {
				firstInstance = null;
				// Show connections page since we're getting rid of the main window
				IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				//IViewPart part = page.findView(ConnectionView.VIEW_ID);
				try {
					if (page != null) 
						page.showView(ConnectionView.VIEW_ID);
				} catch (PartInitException e) {
					e.printStackTrace();
				}
			} else
				firstInstance = openViews.get(0);
		}
		super.dispose();
	}
	
	public void pageDown() {
		streamText.pageDown();
	}
	
	public void pageUp() {
		streamText.pageUp();
	}
	
	public void setClient(IWarlockClient client) {
		this.client = client;
		client.setViewer(wrapper);
		SWTStreamListener streamListener = new SWTStreamListener(streamText);
		client.getDefaultStream().addStreamListener(streamListener);
		streamText.getTitle().addListener(new PropertyListener<String>() {
			public void propertyChanged(String value) {
				setViewTitle(value);
			}
		});
		streamText.setClient(client);
	}
	
	protected abstract void setViewTitle(String title);
	
	public boolean isStreamOpen(String streamName) {
		if(streamName == IWarlockClient.DEFAULT_STREAM_NAME)
			return true;
		
		for(StreamView streamView : StreamView.getOpenViews()) {
			if(streamView.getStreamName().equals(streamName))
				return true;
		}
		return false;
	}
}
