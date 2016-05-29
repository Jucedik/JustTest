package com.crazyhitty.chdev.ks.rssmanager;

import android.os.AsyncTask;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import java.io.IOException;

/**
 * Created by Kartik_ch on 11/15/2015.
 */
public class RssParser extends AsyncTask<String, Integer, String> {

    private Elements mItems;
    private String mUrl;
    private OnFeedLoadListener mOnFeedLoadListener;
    private boolean parseOnlyProviderInfo;

    private String title;
    private String description;
    private String imageUrl;

    public RssParser(String url, OnFeedLoadListener onFeedLoadListener, boolean parseOnlyProviderInfo) {
        this.mUrl = url;
        this.mOnFeedLoadListener = onFeedLoadListener;
        this.parseOnlyProviderInfo = parseOnlyProviderInfo;
    }

    @Override
    protected String doInBackground(String... strings) {
        try {
            Document rssDocument = Jsoup.connect(mUrl).ignoreContentType(true).parser(Parser.xmlParser()).get();

            if (parseOnlyProviderInfo) {
                title = rssDocument.select("title").first().text();
                description = rssDocument.select("description").first().text();

                Elements imageElems = rssDocument.select("image");
                if (imageElems != null && !imageElems.isEmpty()) {
                    Element image = imageElems.first();
                    if (image.select("url") != null) {
                        imageUrl = image.select("url").text();
                    }
                }

                return "success";
            }
            else {
                mItems = rssDocument.select("item");
            }
        }
        catch (Exception e) {
            return "failure";
        }

        return "success";
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        if (s.equals("success")) {
            if (parseOnlyProviderInfo) {
                mOnFeedLoadListener.onSuccessProviderInfo(title, description, imageUrl);
            }
            else {
                mOnFeedLoadListener.onSuccess(mItems);
            }
        }
        else if (s.equals("failure")) {
            mOnFeedLoadListener.onFailure("Failed to parse the url\n" + mUrl);
        }
    }
}
