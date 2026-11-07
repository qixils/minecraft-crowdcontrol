package dev.qixils.crowdcontrol.plugin.fabric.client.vote;

import net.minecraft.voting.votes.ClientVote;
import net.minecraft.voting.votes.OptionId;

import java.util.Map;

public record OptionWrapper(OptionId id, ClientVote.ClientOption data) {
	public OptionWrapper(Map.Entry<OptionId, ClientVote.ClientOption> entry) {
		this(entry.getKey(), entry.getValue());
	}
}
