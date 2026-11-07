package dev.qixils.crowdcontrol.plugin.fabric.client.vote;

import com.mojang.blaze3d.vertex.PoseStack;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.voting.ClientVoteStorage;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.lang.Math.max;
import static net.minecraft.util.Mth.ceil;

public class ProposalHud {
	public final ProposalHandler handler;
	private static final int MARGIN = 6;
	private static final int PADDING = 4;
	private static final int TEXT_SIZE = 10;
	private static final float HEADER_TEXT_SCALE = 1.25f;
	private static final float BODY_TEXT_SCALE = 1.0f;
	private static final int TEXT_MARGIN = 2;
	private static final int TEXT_COLOR = 0xFFFFFF;
	private static final int WINNING_TEXT_COLOR = NamedTextColor.GREEN.value();
	private static final int BACKGROUND_COLOR = 0x60000000;
	private static final int TIME_BAR_COLOR = NamedTextColor.GRAY.value() | 0xFF000000;
	private static final int COOLDOWN_BAR_COLOR = NamedTextColor.DARK_GRAY.value() | 0xFF000000;
	private static final int VOTE_BAR_COLOR = NamedTextColor.YELLOW.value() | 0xFF000000;
	private static final int BAR_HEIGHT = 2;
	private static final int BAR_MIN_WIDTH = 10;
	private static final int PERCENT_PADDING = 6;
	private static final int PERCENT_COLOR = NamedTextColor.GRAY.value();
	private static final double TEXT_MAX_WIDTH = 0.25; // 25% of screen width
	private static final float EXPLANATORY_TEXT_SCALE = 0.75f;
	private static final int EXPLANATORY_TEXT_COLOR = NamedTextColor.GRAY.value();
	private static final Component EXPLANATORY_TEXT_LETTERS = Component.translatableWithFallback("crowdcontrol.proposal.explanatory.letters", "Type the letter of the option you want in chat!");
	private static final Component EXPLANATORY_TEXT_NUMBERS = Component.translatableWithFallback("crowdcontrol.proposal.explanatory.numbers", "Type the number of the option you want in chat!");
	private static final Component EXPLANATORY_TEXT_CLOSED = Component.translatableWithFallback("crowdcontrol.proposal.explanatory.closed", "Final results");

	public ProposalHud(ProposalHandler handler) {
		this.handler = handler;
	}

	private static Component optionText(String voteCommand, OptionWrapper option) {
		return Component.translatable("options.generic_value", voteCommand, option.data().displayName());
	}

	private static Component optionVotesText(int votes, int totalVotes) {
		return Component.literal(ceil(votes / (float) totalVotes * 100f) + "% (" + votes + ")");
	}

	public void render(PoseStack poseStack, Font font, Minecraft client) {
		ProposalVote vote = handler.getCurrentProposal();
		if (vote == null)
			return;
		ClientVoteStorage.VoteInfo proposal = vote.getProposal();
		if (proposal == null)
			return;
		int secondsLeft = max(ceil(vote.getRemainingTicks() / 20f), 0);
		Map<String, Integer> votes = new HashMap<>(vote.voteCounts());
		int totalVotes = max(1, votes.values().stream().mapToInt(Integer::intValue).sum());
		poseStack.pushPose();
		poseStack.translate(MARGIN, MARGIN, 0);
		// render background ...
		Component explanatoryText;
		if (vote.isClosed())
			explanatoryText = EXPLANATORY_TEXT_CLOSED;
		else if (vote.isNumbered)
			explanatoryText = EXPLANATORY_TEXT_NUMBERS;
		else
			explanatoryText = EXPLANATORY_TEXT_LETTERS;
		MutableComponent proposalText = proposal.voteInfo().header().displayName().copy();
		if (secondsLeft > 0)
			proposalText.append(" (" + secondsLeft + "s)");
		int maxWidth = ceil(TEXT_MAX_WIDTH * client.getWindow().getGuiScaledWidth());
		int optionWidth = 0;
		int optionHeight = 0;
		for (Map.Entry<String, OptionWrapper> entry : vote.getOptions().entrySet()) {
			OptionWrapper option = entry.getValue();
			List<FormattedCharSequence> lines = font.split(optionText(entry.getKey(), option), maxWidth);
			int width = lines.stream().mapToInt(font::width).max().orElse(0);
			int height = lines.size() * TEXT_SIZE;
			optionWidth = max(optionWidth, width);
			optionHeight += height;
		}
		int optionVotesWidth = font.width(optionVotesText(totalVotes, totalVotes));
		optionWidth += PERCENT_PADDING + optionVotesWidth;
		int contentWidth = max(max(ceil(font.width(proposalText) * HEADER_TEXT_SCALE), ceil(optionWidth * BODY_TEXT_SCALE)), ceil(font.width(explanatoryText) * EXPLANATORY_TEXT_SCALE));
		int height = (PADDING * 2) + ceil(TEXT_SIZE * HEADER_TEXT_SCALE) + BAR_HEIGHT + TEXT_MARGIN + ceil((((BAR_HEIGHT + TEXT_MARGIN) * vote.getOptions().size()) + optionHeight) * BODY_TEXT_SCALE) + ceil(TEXT_SIZE * EXPLANATORY_TEXT_SCALE);
		GuiComponent.fill(poseStack, 0, 0, contentWidth + (PADDING * 2), height, BACKGROUND_COLOR);
		// render text ...
		poseStack.translate(PADDING, PADDING, 0);
		poseStack.scale(HEADER_TEXT_SCALE, HEADER_TEXT_SCALE, 1);
		font.drawShadow(poseStack, proposalText, 0, 0, TEXT_COLOR);
		poseStack.translate(0, TEXT_SIZE, 0);
		poseStack.scale(EXPLANATORY_TEXT_SCALE / HEADER_TEXT_SCALE, EXPLANATORY_TEXT_SCALE / HEADER_TEXT_SCALE, 1);
		font.drawShadow(poseStack, explanatoryText, 0, 0, EXPLANATORY_TEXT_COLOR);
		poseStack.translate(0, TEXT_SIZE, 0);
		poseStack.scale(1 / EXPLANATORY_TEXT_SCALE, 1 / EXPLANATORY_TEXT_SCALE, 1);
		int cooldownBarWidth = vote.isClosed()
				? ceil(contentWidth * (handler.proposalCooldown / (float) ProposalVote.COOLDOWN))
				: contentWidth;
		int timeBarWidth = ceil(contentWidth * vote.getRemainingTimePercentage());
		GuiComponent.fill(poseStack, 0, 0, cooldownBarWidth, BAR_HEIGHT, COOLDOWN_BAR_COLOR);
		GuiComponent.fill(poseStack, 0, 0, timeBarWidth, BAR_HEIGHT, TIME_BAR_COLOR);
		poseStack.translate(0, BAR_HEIGHT + (2 * TEXT_MARGIN), 0);
		poseStack.scale(BODY_TEXT_SCALE, BODY_TEXT_SCALE, 1);
		String winnerKey = vote.isClosed() ? vote.getTopOptionKey() : null;
		for (Map.Entry<String, OptionWrapper> entry : vote.getOptions().entrySet()) {
			String voteCommand = entry.getKey();
			OptionWrapper option = entry.getValue();
			int votesForOption = votes.getOrDefault(voteCommand, 0);
			// draw text
			int adjustedWidth = ceil((contentWidth - PERCENT_PADDING - optionVotesWidth) / BODY_TEXT_SCALE);
			Component optionVotesText = optionVotesText(votesForOption, totalVotes);
			font.drawShadow(poseStack, optionVotesText, ceil((contentWidth - font.width(optionVotesText)) / BODY_TEXT_SCALE), 0, PERCENT_COLOR);
			for (FormattedCharSequence line : font.split(optionText(voteCommand, option), adjustedWidth)) {
				font.drawShadow(poseStack, line, 0, 0, Objects.equals(winnerKey, voteCommand) ? WINNING_TEXT_COLOR : TEXT_COLOR);
				poseStack.translate(0, TEXT_SIZE, 0);
			}
			// draw bar
			int barWidth = BAR_MIN_WIDTH + ceil(((contentWidth / BODY_TEXT_SCALE) - BAR_MIN_WIDTH) * (votesForOption / (float) totalVotes));
			GuiComponent.fill(poseStack, 0, 0, barWidth, BAR_HEIGHT, VOTE_BAR_COLOR);
			// translate
			poseStack.translate(0, BAR_HEIGHT + TEXT_MARGIN, 0);
		}
		poseStack.popPose();
	}
}
