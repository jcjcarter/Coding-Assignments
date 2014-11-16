package com.seanharger.chatapp.controller;

import java.awt.Component;
import java.util.List;

import com.seanharger.chatapp.model.ChatAppModel;
import com.seanharger.chatapp.model.IModelToViewAdapter;
import com.seanharger.chatapp.model.chatroom.Chatroom;
import com.seanharger.chatapp.model.chatroom.IChatroomToViewAdapter;
import com.seanharger.chatapp.view.IViewToModelAdapter;
import com.seanharger.chatapp.view.MainFrame;
import com.seanharger.chatapp.view.roompanel.ChatroomPanel;
import com.seanharger.chatapp.view.roompanel.IViewToChatroomAdapter;

import common.chatroom.IChatroomID;

/**
 * Main Controller for the ChatApp application.
 * 
 * @author Jayson Carter, Sean Harger
 *
 */
public class ChatApp {

  /** The main GUI frame */
  private MainFrame mainFrame;
  /** The main ChatApp model */
  private ChatAppModel model;

  /**
   * Constructs a new ChatApp controller and initializes the adapters between the models and views.
   */
  public ChatApp() {
    model = new ChatAppModel(new IModelToViewAdapter() {

      @Override
      public IChatroomToViewAdapter makeChatroomToViewAdapter(Chatroom room) {
        ChatroomPanel chatroomPanel =
            mainFrame.makeChatroomPanel(room.getName(), IViewToChatroomAdapter.NULL_OBJECT);
        chatroomPanel.installAdapter(new IViewToChatroomAdapter() {

          @Override
          public void send(String message) {
            room.sendMessage(message);
          }

          @Override
          public void leave() {
            room.leaveRoom();
          }

          @Override
          public void sendUnknown() {
            room.sendUnknown();
          }

        });

        return new IChatroomToViewAdapter() {

          @Override
          public void displayMessage(String message) {
            chatroomPanel.appendTextToFrame(message);
          }

          @Override
          public void removeChatroom() {
            mainFrame.removeChatroomPanel(chatroomPanel);
          }

          @Override
          public void addComponent(Component comp) {
            chatroomPanel.addComponent(comp);
          }

          @Override
          public void showErrorDialog(String title, String errorMessage) {
            // TODO Auto-generated method stub
            
          }

        };
      }

      @Override
      public void showInformationDialog(String title, String message) {
        mainFrame.showInformationDialog(title, message);
      }

      @Override
      public boolean displayQuestionMessage(String title, String question) {
        return mainFrame.displayQuestionMessage(title, question);
      }

      @Override
      public void showErrorDialog(String title, String errorMessage) {
        mainFrame.showErrorDialog(title, errorMessage);
      }

      @Override
      public IChatroomID chooseChatroomToJoin(List<IChatroomID> chatroomIds) {
        return mainFrame.chooseChatroomToJoin(chatroomIds);
      }

      @Override
      public Chatroom chooseChatroomToInvite(List<Chatroom> chatrooms) {
        return mainFrame.chooseChatroomToInvite(chatrooms);
      }

    });

    mainFrame = new MainFrame(new IViewToModelAdapter() {

      @Override
      public void createChatroom(String name) {
        model.makeRoom(name);
      }

      @Override
      public void requestToJoin(String ipAddress) {
        model.requestToJoin(ipAddress);
      }

      @Override
      public void inviteToChatroom(String ipAddress) {
        model.inviteToChatroom(ipAddress);
      }

      @Override
      public void quit() {
        model.leaveAllChatrooms();
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
        model.stop();
      }

    });

  }

  /**
   * Starts the model and view.
   */
  public void start() {
    mainFrame.start();
    model.start();
  }

  /**
   * Main runner for the ChatApp application. Creates the ChatApp controller and starts it.
   * 
   * @param args unused for this application
   */
  public static void main(String[] args) {
    System.setProperty("apple.laf.useScreenMenuBar", "true");
    System.setProperty("com.apple.mrj.application.apple.menu.about.name", "ChatApp");
    (new ChatApp()).start();
  }
}