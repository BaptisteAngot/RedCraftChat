package org.redcraft.redcraftchat.translate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.redcraft.redcraftchat.Config;
import org.redcraft.redcraftchat.database.PlayerPreferencesManager;
import org.redcraft.redcraftchat.detection.DetectionManager;
import org.redcraft.redcraftchat.models.deepl.DeeplResponse;
import org.redcraft.redcraftchat.models.modernmt.ModernmtResponse;
import org.redcraft.redcraftchat.models.translate.TokenizedMessage;
import org.redcraft.redcraftchat.tokenizer.TokenizerManager;
import org.redcraft.redcraftchat.translate.services.DeeplClient;
import org.redcraft.redcraftchat.translate.services.ModernmtClient;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class TranslationManager {
    public static String translate(String text, String sourceLanguage, String targetLanguage) throws Exception {
        if (!Config.translationEnabled) {
            throw new Exception("TranslationManager was called but translation is disabled in the configuration");
        }

        TokenizedMessage tokenizedMessage = TokenizerManager.tokenizeElements(text, true);

        switch (Config.translationService) {
            case "deepl":
                DeeplResponse dr = DeeplClient.translate(tokenizedMessage.tokenizedMessage, sourceLanguage.toUpperCase(), targetLanguage.toUpperCase());
                tokenizedMessage.tokenizedMessage = DeeplClient.parseDeeplResponse(dr);
                break;
            case "modernmt":
                ModernmtResponse mr = ModernmtClient.translate(tokenizedMessage.tokenizedMessage, sourceLanguage.toLowerCase(), targetLanguage.toLowerCase());
                tokenizedMessage.tokenizedMessage = mr.data.translation;
                break;
            default:
                throw new Exception(String.format("Unknown translation service \"%s\"", Config.translationService));
        }

        return TokenizerManager.untokenizeElements(tokenizedMessage);
    }

    // TODO parallelize
    public static Map<String, String> translateBulk(String text, String sourceLanguage, List<String> targetLanguages) {
        Map<String, String> translatedLanguages = new HashMap<String, String>();

        for (String targetLanguage : targetLanguages) {
            if (targetLanguage.equalsIgnoreCase(sourceLanguage)) {
                translatedLanguages.put(targetLanguage, text);
                continue;
            }
            try {
                translatedLanguages.put(targetLanguage, TranslationManager.translate(text, sourceLanguage, targetLanguage));
            } catch (Exception e) {
                translatedLanguages.put(targetLanguage, text);
                e.printStackTrace();
            }
        }

        return translatedLanguages;
    }

    public static String getSourceLanguage(String message, ProxiedPlayer sender) {
        String sourceLanguage = DetectionManager.getLanguage(message);

        if (sourceLanguage == null) {
            sourceLanguage = PlayerPreferencesManager.getMainPlayerLanguage(sender);
        }

        return sourceLanguage;
    }

    public static List<String> getTargetLanguages(String sourceLanguage) {
        List<String> targetLanguages = new ArrayList<String>(Config.translationSupportedLanguages);

        for (ProxiedPlayer receiver : ProxyServer.getInstance().getPlayers()) {
            if (!PlayerPreferencesManager.playerSpeaksLanguage(receiver, sourceLanguage)) {
                String playerLanguage = PlayerPreferencesManager.getMainPlayerLanguage(receiver).toLowerCase();
                if (!targetLanguages.contains(playerLanguage)) {
                    targetLanguages.add(playerLanguage);
                }
            }
        }

        return targetLanguages;
    }

    // Get stuff like EN->FR
    public static String getLanguagePrefix(String sourceLanguage, String targetLanguage) {
        String languagePrefix = sourceLanguage.toUpperCase();

        if (!targetLanguage.equalsIgnoreCase(sourceLanguage)) {
            languagePrefix += "➔" + targetLanguage.toUpperCase();
        }

        return languagePrefix;
    }
}
