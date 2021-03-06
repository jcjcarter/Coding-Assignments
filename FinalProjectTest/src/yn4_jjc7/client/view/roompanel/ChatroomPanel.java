package yn4_jjc7.client.view.roompanel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

/**
 * The mini-view for an individual chatroom. This is contained within a <code>MainFrame</code>.
 */
public class ChatroomPanel extends JPanel {

	/** Serial ID */
	private static final long serialVersionUID = -4520690904050470438L;

	/** The adapter to the chatroom model */
	private IViewToChatroomAdapter chatroom;

	/* UI Components */
	private final Box chatBox = Box.createVerticalBox();
	private final JPanel panelChatroomActions = new JPanel();
	private final JTextField textFieldMessage = new JTextField();
	private final JButton btnSend = new JButton("Send");
	private final JButton btnLeave = new JButton("Leave");
	private final JScrollPane scrollPane = new JScrollPane(chatBox, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
			JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	private final JButton btnDonald = new JButton("Donald");
	private final JButton btnMap = new JButton("Map");

	/**
	 * Create the panel.
	 * 
	 * @param chatroom the adapter to the chatroom model
	 */
	public ChatroomPanel(IViewToChatroomAdapter chatroom) {
		this.chatroom = chatroom;
		initGUI();
	}

	/**
	 * Installs the specified adapter to communicate with the model.
	 * 
	 * @param chatroom the adapter to install
	 */
	public void installAdapter(IViewToChatroomAdapter chatroom) {
		this.chatroom = chatroom;
	}

	/**
	 * Appends a text string to the end of the main text frame.
	 * 
	 * @param message the message to append
	 */
	public void appendTextToFrame(String message) {
		addComponent(new JLabel(message));
	}

	/**
	 * Appends a generic component to the end of the main text frame.
	 * 
	 * @param comp the component to add
	 */
	public void addComponent(Component comp) {
		chatBox.add(comp);
		scrollPane.paintAll(getGraphics());
		scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getMaximum());
		repaint();
	}

	/**
	 * Sends the message in the text field by calling the model's send method.
	 */
	private void sendTextInBox() {
		chatroom.send(textFieldMessage.getText());
		textFieldMessage.setText("");
	}

	/**
	 * Initializes the GUI.
	 */
	public void initGUI() {

		setLayout(new BorderLayout(0, 0));

		add(panelChatroomActions, BorderLayout.SOUTH);
		textFieldMessage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				sendTextInBox();
			}
		});
		textFieldMessage.setToolTipText("Type a message to send to chatroom.");

		textFieldMessage.setColumns(20);
		panelChatroomActions.add(textFieldMessage);
		btnSend.setToolTipText("Send message to chatroom.");
		btnSend.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				sendTextInBox();
			}
		});

		panelChatroomActions.add(btnSend);
		btnLeave.setToolTipText("Leave the chatroom.");

		panelChatroomActions.add(btnLeave);
		btnDonald.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				chatroom.sendDonald();
			}
		});

		panelChatroomActions.add(btnDonald);
		btnMap.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				chatroom.sendMap();
			}
		});
		panelChatroomActions.add(btnMap);
		btnLeave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				chatroom.leave();
			}
		});

		add(scrollPane, BorderLayout.CENTER);
	}

}
