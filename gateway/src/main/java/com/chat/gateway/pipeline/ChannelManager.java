package com.chat.gateway.pipeline;

import io.netty.channel.Channel;
import io.netty.channel.ChannelId;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ChannelManager {

    private final ConcurrentHashMap<String, List<Channel>> userChannels;
    private static volatile ChannelManager instance;

    private ChannelManager() {
        this.userChannels = new ConcurrentHashMap<>();
    }

    public static ChannelManager getInstance() {
        if (instance == null) {
            synchronized (ChannelManager.class) {
                if (instance == null) {
                    instance = new ChannelManager();
                }
            }
        }
        return instance;
    }

    public void addChannel(String userId, Channel channel) {
        userChannels.compute(userId, (key, channels) -> {
            if (channels == null) {
                channels = new ArrayList<>();
            }
            if (!channels.contains(channel)) {
                channels.add(channel);
            }
            return channels;
        });
    }

    public void removeChannel(String userId, Channel channel) {
        userChannels.computeIfPresent(userId, (key, channels) -> {
            channels.remove(channel);
            if (channels.isEmpty()) {
                return null;
            }
            return channels;
        });
    }

    public List<Channel> getChannels(String userId) {
        List<Channel> channels = userChannels.get(userId);
        if (channels == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(new ArrayList<>(channels));
    }

    public boolean hasChannels(String userId) {
        List<Channel> channels = userChannels.get(userId);
        return channels != null && !channels.isEmpty();
    }

    public int getUserCount() {
        return userChannels.size();
    }

    public void removeAllChannelsForUser(String userId) {
        userChannels.remove(userId);
    }

    public void clear() {
        userChannels.clear();
    }
}
