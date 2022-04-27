package com.example.mycoin.miner;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import static com.example.mycoin.miner.Message.MESSAGE_TYPE.*;

public class MinerServerThread extends Thread {
    private Socket client;
    private final Miner miner;

    MinerServerThread(final Miner miner, final Socket client) {
        super(miner.getName() + System.currentTimeMillis());
        this.miner = miner;
        this.client = client;
    }

    @Override
    public void run() {
        try (
            ObjectOutputStream out = new ObjectOutputStream(client.getOutputStream());
            final ObjectInputStream in = new ObjectInputStream(client.getInputStream())) {
            Message message = new Message.MessageBuilder().withSender(miner.getPort()).withType(READY).build();
            out.writeObject(message);
            Object fromClient;
            while ((fromClient = in.readObject()) != null) {
                if (fromClient instanceof Message) {
                    final Message msg = (Message) fromClient;
                    System.out.println(String.format("%d received: %s", miner.getPort(), fromClient.toString()));
                    if (INFO_NEW_BLOCK == msg.type) {
                        if (msg.blocks.isEmpty() || msg.blocks.size() > 1) {
                            System.err.println("Invalid block received: " + msg.blocks);
                        }
                        synchronized (miner) {
                            miner.addBlock(msg.blocks.get(0));
                        }
                        break;
                    } else if (REQ_ALL_BLOCKS == msg.type) {
                        out.writeObject(new Message.MessageBuilder()
                                .withSender(miner.getPort())
                                .withType(RSP_ALL_BLOCKS)
                                .withBlocks(miner.getBlockchain())
                                .build());
                        break;
                    }
                }
            }
            client.close();
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
    }
}
