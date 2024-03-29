package com.gsrmeen.itemcrawler;

import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class PixelmonItemCrawler implements IItemCrawler {
    private String item;
    private Document document;

    private static String URL_TEMPLATE = "https://pixelmonmod.com/wiki/index.php?title=";
    private static final String ERROR_DIV = "noarticletext mw-content-ltr";


    public PixelmonItemCrawler(String item) {
        this.item = item;
        try {
            this.document = Jsoup.connect(getUrl()).get();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public TextComponentString getDescription() {
        Elements texts = document.selectFirst("div.mw-parser-output").select("p");
        Element content = null;
        for (Element e : texts) {
            if (e.select("i").size() == 0) {
                content = e;
                break;
            }
        }

        TextComponentString message = new TextComponentString("");

        TextComponentString header = new TextComponentString(item + ": ");
        header.setStyle(new Style().setColor(TextFormatting.GOLD).setItalic(true));
        message.appendSibling(header);
        message.appendSibling(new TextComponentString(content.text()));
        return message;
    }

    @Override
    public TextComponentString getPossibleDrops() {
        Elements allElems = document.body().getAllElements();
        int i;
        boolean titleFound = false;
        for (i = 0; i < allElems.size(); i++) {
            Element e = allElems.get(i);
            if (titleFound && e.is("table")) {
                break;
            }
            if (e.id().equals("Pok.C3.A9mon_drops")) {
                titleFound = true;
            }
        }

        if (i == allElems.size()) {
            return null;
        }
        
        Elements tableRows = allElems.get(i).selectFirst("tbody").select("tr");

        StringBuilder sb = new StringBuilder();
        for (int j = 1; j < tableRows.size(); j++) {
            Element row = tableRows.get(j);
            Elements cells = row.select("td");
            String pokemon = cells.get(0).text();
            String chance = cells.get(1).text();
            sb.append(String.format("(%s : %s) ", pokemon, chance));
        }
        return new TextComponentString(sb.toString());
    }

    @Override
    public boolean itemValid() {
        if (document == null) return false;
        Elements faultyElems = document.select("div[style='" + ERROR_DIV + "']");
        return faultyElems != null && faultyElems.size() == 0;
    }


    private String getUrl() {
        return URL_TEMPLATE + item.replace(" ", "_");
    }
}
