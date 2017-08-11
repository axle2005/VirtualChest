package com.github.ustc_zzzz.virtualchest.placeholder;

import com.github.ustc_zzzz.virtualchest.VirtualChestPlugin;
import com.github.ustc_zzzz.virtualchest.translation.VirtualChestTranslation;
import com.github.ustc_zzzz.virtualchest.unsafe.PlaceholderAPIUtils;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TextRepresentable;
import org.spongepowered.api.text.TextTemplate;
import org.spongepowered.api.text.serializer.TextSerializer;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author ustc_zzzz
 */
public class VirtualChestPlaceholderManager
{
    private static final String ARG_BOUNDARY = "%";
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("[%]([^%]+)[%]", Pattern.CASE_INSENSITIVE);

    private final Logger logger;

    public VirtualChestPlaceholderManager(VirtualChestPlugin plugin)
    {
        this.logger = plugin.getLogger();
    }

    public void init()
    {
        this.logger.info("Try to load the PlaceholderAPI service ... ");
        if (!PlaceholderAPIUtils.isPlaceholderAPIAvailable())
        {
            this.logger.warn("VirtualChest could not find the PlaceholderAPI service. ");
            this.logger.warn("Features related to PlaceholderAPI may not work normally. ");
            this.logger.warn("Maybe you should look for a PlaceholderAPI plugin and download it?");
        }
    }

    public String parseText(Player player, String text)
    {
        return this.replace(player, text, Function.identity());
    }

    private TextTemplate toTemplate(String text)
    {
        List<TextRepresentable> parts = new LinkedList<>();
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(text);
        int lastIndex = 0;
        while (matcher.find())
        {
            parts.add(TextSerializers.PLAIN.deserialize(text.substring(lastIndex, matcher.start())));
            parts.add(TextTemplate.arg(text.substring(matcher.start() + 1, matcher.end() - 1)).build());
            lastIndex = matcher.end();
        }
        if (lastIndex < text.length())
        {
            parts.add(Text.builder(text.substring(lastIndex)).build());
        }
        return TextTemplate.of(ARG_BOUNDARY, ARG_BOUNDARY, parts.toArray());
    }

    private String replace(Player player, String textToBeReplaced, Function<? super String, String> transformation)
    {
        Map<String, Object> args = new HashMap<>();
        TextTemplate template = this.toTemplate(textToBeReplaced);
        for (Map.Entry<String, Object> entry : PlaceholderAPIUtils.fillPlaceholders(template, player).entrySet())
        {
            String replacement = TextSerializers.PLAIN.serialize(Text.of(entry.getValue()));
            args.put(entry.getKey(), transformation.apply(replacement));
        }
        return TextSerializers.PLAIN.serialize(template.apply(args).build());
    }
}