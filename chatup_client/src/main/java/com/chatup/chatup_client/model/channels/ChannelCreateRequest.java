package com.chatup.chatup_client.model.channels;

import java.util.Set;

public record ChannelCreateRequest(
        String name,
        Boolean is_private,
        Boolean is_direct_message,
        Set<Long> user_ids
) {}
