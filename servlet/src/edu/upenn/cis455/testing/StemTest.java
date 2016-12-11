package edu.upenn.cis455.testing;


import edu.upenn.cis455.querying.Stemmer;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class StemTest{

    public static void main(String[] args) {

        String xw = args[0];
        Stemmer stemmer = new Stemmer();

        /*
        stemmer.add(w.toCharArray(), w.length());
        stemmer.stem();
        String stemmedWord = stemmer.toString();
        System.out.println("WORD:" + w);
        System.out.println("STEMMED WORD:" + stemmedWord);
        String w2 = args[1];
        stemmer.add(w2.toCharArray(), w.length());
        stemmer.stem();
        String stemmedWord2 = stemmer.toString();
        System.out.println("WORD:" + w2);
        System.out.println("STEMMED WORD:" + stemmedWord2);
        */

        String documenttext = Jsoup.parse("\n" +
                "<!doctype html>\n" +
                "<head>\n" +
                "<script>\n" +
                "  (function (H){\n" +
                "  H.className=H.className.replace(/\\bno-js\\b/,'js');\n" +
                "  if (('; '+document.cookie).match(/; _ted_user_id=/)) H.className=H.className.replace(/\\bloggedout\\b/,'loggedin');\n" +
                "  })(document.documentElement)\n" +
                "</script><meta charset='utf-8'>\n" +
                "<title>TED Fellows Program | Programs &amp; Initiatives | About | TED.com</title>\n" +
                "<meta content=\"The TED Fellows program invites innovators from around the globe to become part of the TED community, and amplifies the impact of their remarkable work.\" name=\"description\" />\n" +
                "<meta content=\"TED, TED fellows, speakers, talks, tedfellows, tedfellow, fellows application, ted fellows application\" name=\"keywords\" />\n" +
                "<meta content=\"http://fellowsblog.ted.com/wp-content/uploads/2014/04/Screen-Shot-2014-04-16-at-12.44.09.png?1397660094\" name=\"thumbnail\" />\n" +
                "<meta content=\"http://fellowsblog.ted.com/wp-content/uploads/2014/04/Screen-Shot-2014-04-16-at-12.44.09.png?1397660094\" property=\"og:image\" />\n" +
                "<link color=\"#E62B1E\" href=\"https://pa.tedcdn.com/mask-icon.svg\" rel=\"mask-icon\" sizes=\"any\" />\n" +
                "<meta content=\"#E62B1E\" name=\"theme-color\" />\n" +
                "<link href=\"https://pa.tedcdn.com/favicon.ico\" rel=\"shortcut icon\" />\n" +
                "<meta content=\"True\" name=\"HandheldFriendly\" />\n" +
                "<meta content=\"320\" name=\"MobileOptimized\" />\n" +
                "<meta content=\"width=device-width, initial-scale=1.0\" name=\"viewport\" />\n" +
                "<meta content=\"TED.com\" name=\"apple-mobile-web-app-title\" />\n" +
                "<meta content=\"yes\" name=\"apple-mobile-web-app-capable\" />\n" +
                "<meta content=\"black\" name=\"apple-mobile-web-app-status-bar-style\" />\n" +
                "<link href=\"https://pa.tedcdn.com/apple-touch-icon.png\" rel=\"apple-touch-icon\" />\n" +
                "<link href=\"https://pa.tedcdn.com/apple-touch-icon-precomposed.png\" rel=\"apple-touch-icon-precomposed\" />\n" +
                "<meta content=\"TED.com\" name=\"application-name\" />\n" +
                "<meta content=\"/browserconfig.xml\" name=\"msapplication-config\" />\n" +
                "<meta content=\"#000000\" name=\"msapplication-TileColor\" />\n" +
                "<meta content=\"on\" http-equiv=\"cleartype\" />\n" +
                "<meta content=\"TED Fellows Program\" name=\"title\" />\n" +
                "<meta content=\"TED Fellows Program\" property=\"og:title\" />\n" +
                "<meta content=\"The TED Fellows program invites innovators from around the globe to become part of the TED community, and amplifies the impact of their remarkable work.\" property=\"og:description\" />\n" +
                "<meta content=\"http://www.ted.com/about/programs-initiatives/ted-fellows-program\" property=\"og:url\" />\n" +
                "<meta content=\"201021956610141\" property=\"fb:app_id\" /><!--[if lte IE 8]>\n" +
                "<link rel=\"stylesheet\" href=\"https://pa.tedcdn.com/stylesheets/global-ie.css?1480618736\">\n" +
                "<link rel=\"stylesheet\" href=\"https://pa.tedcdn.com/stylesheets/pages-ie.css?1480618736\">\n" +
                "<![endif]-->\n" +
                "<!--[if gt IE 8]><!-->\n" +
                "<link rel=\"stylesheet\" href=\"https://pa.tedcdn.com/stylesheets/global.css?1480618736\">\n" +
                "<link rel=\"stylesheet\" href=\"https://pa.tedcdn.com/stylesheets/pages.css?1480618736\">\n" +
                "<!--<![endif]-->\n" +
                "<!--[if lte IE 8]>\n" +
                "<link rel=\"stylesheet\" href=\"https://pa.tedcdn.com/stylesheets/shed-ie.css?1480618736\">\n" +
                "<link rel=\"stylesheet\" href=\"https://pa.tedcdn.com/stylesheets/pages-ie.css?1480618736\">\n" +
                "<![endif]-->\n" +
                "<!--[if gt IE 8]><!-->\n" +
                "<link rel=\"stylesheet\" href=\"https://pa.tedcdn.com/stylesheets/shed.css?1480618736\">\n" +
                "<link rel=\"stylesheet\" href=\"https://pa.tedcdn.com/stylesheets/pages.css?1480618736\">\n" +
                "<!--<![endif]--><script>\n" +
                "  if(top != self) top.location.replace(location);\n" +
                "</script><script>\n" +
                "  (function(i,r,l,d,o){\n" +
                "    i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){(i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();\n" +
                "    if(l && d!=\"yes\" && d!=\"1\") o.userId=l[1];\n" +
                "    __ga('create',\"UA-40781755-2\",'ted.com',o);\n" +
                "    __ga('set',\"dimension3\",'logged'+(l ? 'In' : 'Out'));\n" +
                "  })(window,\"__ga\",('; '+document.cookie).match(/; _ted_user_id=(\\d+);/),(window.navigator && window.navigator.doNotTrack),{});\n" +
                "</script><script>\n" +
                "  var googletag = googletag || {};\n" +
                "  googletag.cmd = googletag.cmd || [];\n" +
                "  \n" +
                "  googletag.cmd.push(function() {\n" +
                "    googletag.pubads().enableAsyncRendering();\n" +
                "    googletag.enableServices();\n" +
                "  });\n" +
                "  \n" +
                "  function gptProxy(data) {\n" +
                "    g('ads.gptAd', data);\n" +
                "  }\n" +
                "</script><script>\n" +
                "  _q=[];q=function(){_q.push(arguments)};\n" +
                "  _g=[];g=function(){_g.push(arguments)};\n" +
                "  \n" +
                "  TED = {\"assetBuster\":1480618736,\"playerPath\":\"//pb.tedcdn.com/assets/player/flash_hls/player_4_00_014.swf\",\"playerPathRemote\":\"//pb.tedcdn.com/assets/player/flash_hls/player_4_00_014.swf\",\"playerPathLocal\":\"/assets/player/flash_hls/player_4_00_014.swf\",\"assetHost\":\"https://pa.tedcdn.com\",\"authHost\":\"https://auth.ted.com\",\"settingsUrl\":\"https://www.ted.com/settings/account\",\"signInUrl\":\"/session/new\",\"signOutUrl\":\"https://auth.ted.com/session/logout\",\"signInHelpUrl\":\"https://auth.ted.com/account/password/new\",\"signUpUrl\":\"/users/new\",\"csClientId\":\"7341760\",\"gaDimensions\":{\"breakpoint\":\"dimension1\",\"talkId\":\"dimension2\",\"authState\":\"dimension3\",\"playlistId\":\"dimension5\",\"testId\":\"dimension7\",\"embedZone\":\"dimension8\",\"gaClientId\":\"dimension9\",\"tedUserId\":\"dimension10\",\"playContext\":\"dimension11\",\"sourceContext\":\"dimension12\",\"playbackRate\":\"dimension13\",\"playerMode\":\"dimension14\"}};\n" +
                "  TED.signOutUrl += '?referer=' + location.protocol + '//' + location.host + '/session/logout';\n" +
                "  \n" +
                "  TED.startTime = new Date();\n" +
                "  TED.isFirstVisit = !document.cookie.match(/; _ga=/);\n" +
                "  \n" +
                "  TED.abby = (function () {\n" +
                "    var abs=(document.cookie.match(/_abby_(\\w+)=(\\w+)/g) || []), dms=[], ts={}, t;\n" +
                "    for (var i=-1, l=abs.length; ++i < l;) {\n" +
                "      t = abs[i].match(/_abby_(\\w+)=(\\w+)/);\n" +
                "      ts[t[1]] = t[2];\n" +
                "      tstr = t[1] + ':' + t[2];\n" +
                "      if (dms.indexOf(tstr) < 0) dms.push(tstr);\n" +
                "    }\n" +
                "    if (dms.length) {\n" +
                "      __ga('set', \"dimension7\", dms.join(','));\n" +
                "    }\n" +
                "    return {tests: ts}\n" +
                "  }());\n" +
                "  \n" +
                "  require = {\"baseUrl\":\"https://pa.tedcdn.com/javascripts\",\"map\":{\"*\":{\"Handlebars\":\"hbs/handlebars\",\"underscore\":\"lodash\",\"vendor/swfobject\":\"swfobject\"}},\"waitSeconds\":0,\"deps\":[\"libs\"],\"paths\":{\"playlists\":\"playlists.js?bf6b49538598dd264f8ef4c0f7ccd38b\",\"Swipe\":\"Swipe.js?15bf5d97c89fe4ab638e56a64a3c2be2\",\"ted-talk-page\":\"ted-talk-page.js?25545be4957e46189cc71855e0284607\",\"tedx\":\"tedx.js?d44373019595308c03681ad91765c2a3\",\"flot\":\"flot.js?144b195b1c330e29b772bce21016ea44\",\"hbs\":\"hbs.js?43bff73c5d922c5ecbdff8188500b45c\",\"transit\":\"transit.js?be699430907fbb3966d08b6998e00bb4\",\"dashboard_attendees\":\"dashboard_attendees.js?dd38a09b2511c06423a74e8d2c7c361d\",\"pages\":\"pages.js?cf60dee78dbe44d0e5cbafb3f333a308\",\"deferLink\":\"deferLink.js?660e8a0a1065819a6915d66ceb6b2e2f\",\"libs\":\"libs.js?98e58d8a8b878fe6bb21529b7b2f4a9d\",\"dashboard_tedx\":\"dashboard_tedx.js?af761ef2925c76662eb436f1b8c29ac5\",\"ted-master-ad-interface\":\"ted-master-ad-interface.js?2f97403b7b94ec756f60fd75fefb621d\",\"redux-saga\":\"redux-saga.js?4f8aa5ddc167169853ceaa749d738bb7\",\"react\":\"react.js?016e175dd8b5ba1741ab6d22f8fa3180\",\"jquery\":\"libs.js?98e58d8a8b878fe6bb21529b7b2f4a9d\",\"swfobject\":\"libs.js?98e58d8a8b878fe6bb21529b7b2f4a9d\",\"comments\":\"comments.js?c2afb9eee2ecc115f755103ea32ad183\",\"Mailcheck\":\"Mailcheck.js?b32eae17e7f8cf0e7da6448de1de2c79\",\"core\":\"core.js?35a49058ee253f8cd154bad099a5c313\",\"jqueryujs\":\"libs.js?98e58d8a8b878fe6bb21529b7b2f4a9d\",\"formatDuration\":\"formatDuration.js?118bed977ad935a5a2d8639e64abbc6c\",\"Backbone\":\"libs.js?98e58d8a8b878fe6bb21529b7b2f4a9d\",\"hlsjs\":\"hlsjs.js?978e4784bdb185c2e771a7407b5850e4\",\"speakingurl\":\"speakingurl.js?76b5e4e4b40b1c416697c6386b9e5a10\",\"clipboard\":\"clipboard.js?3d1e6cfbcc57c6abd5925f6e1a5afd80\",\"swatch\":\"swatch.js?8cea05f28faea769cfb9a65799c0604e\",\"react-redux\":\"react-redux.js?386626400bf17a00fd65ca77302be158\",\"search\":\"search.js?b744104f3c897a7f12d57bfd2e453e7f\",\"profiles\":\"profiles.js?d36787730ad4d64465012cc5686d603b\",\"newsletter\":\"newsletter.js?653c5eb2c903350a62c93ed87bea47f3\",\"talkPageSPA\":\"talkPageSPA.js?f040b92943fd38891a5e088e07e36260\",\"jwplayer/jwplayer.html5\":\"jwplayer/jwplayer.html5.js?c0969ca5364f270ed8647d28c59dc9f9\",\"jwplayer/jwplayer\":\"jwplayer/jwplayer.js?06735c0a9046236cc35a724dc09c5b0d\",\"topics\":\"topics.js?e7ffca21296bb87606252d0deb0cda1e\",\"spinjs\":\"spinjs.js?6ee78060107982392dc8076e165f24e5\",\"redux\":\"redux.js?5c6f3b7455445860057a8b088425d718\",\"classnames\":\"classnames.js?32f2c58b6085bc2c65a80e2eb28b7955\",\"home\":\"home.js?442589288f7a226cc1aaac544b87b042\",\"numeral\":\"numeral.js?160f24725a001040d67d48f5e5c3baf9\",\"waypoints\":\"waypoints.js?aff608778f790deb44f0271497b8702b\",\"abby-tests/sponsorship-2/sponsorship\":\"abby-tests/sponsorship-2/sponsorship.js?1ecee5b8c292a760dcb9092ce6a364fc\",\"abby-tests/talktalk/talk-permalink\":\"abby-tests/talktalk/talk-permalink.js?d61807d3d6f022f2c87e2294434c6e16\",\"abby-tests/talktalk/talk-permalink-old\":\"abby-tests/talktalk/talk-permalink-old.js?c45899d6e99fe5a8b99cd9876d3aaeb9\",\"abby-tests/talktube-2/talk-permalink\":\"abby-tests/talktube-2/talk-permalink.js?df90a4d982e0fae452fc2f80e90ea5fe\",\"abby-tests/talktube-2/talk-permalink-old\":\"abby-tests/talktube-2/talk-permalink-old.js?3754cc4373008282bd014c2851250b33\",\"react-svg-inline\":\"react-svg-inline.js?8f44e09dc3973ffc787605f06b6ab871\",\"react-slick\":\"react-slick.js?73c32938c208abdeb096645a1b002b72\",\"Modernizr\":\"Modernizr.js?d7439d114d161f45ec3a6e1674b66c6d\",\"global\":\"global.js?666c0d760f42b74c426e283a85a4774c\",\"enquire\":\"enquire.js?3f559b6a7cbc6f5d5643f285657fedee\",\"dashboard_hotels\":\"dashboard_hotels.js?733f381e6343ae5e8a00a5e9460c9aef\",\"dashboard\":\"dashboard.js?efd369d0e324960d71cfeef7ce7db59d\",\"dq\":\"libs.js?98e58d8a8b878fe6bb21529b7b2f4a9d\",\"es6\":\"es6.js?67d446f0796af20cd6c37dcfcce8c1b7\",\"pages/dynamic/dummy\":\"pages/dynamic/dummy.js?c4510725de4bbca4fe655b5b33bcf42c\",\"talk\":\"talk.js?a9c120dcc0ef22c4a1fb5ba291136925\",\"lodash\":\"libs.js?98e58d8a8b878fe6bb21529b7b2f4a9d\",\"surprise_me\":\"surprise_me.js?7ecf320ffe0cdc228734cc5068828903\",\"react-dom\":\"react-dom.js?0e4bb58cd36b3da9fefb45e85ae3655d\",\"text\":\"text.js?6448992f97b7a06f78f0b4b46d448f80\",\"SwipeView\":\"SwipeView.js?cd503ab38937431f43dec5f016836328\",\"depot\":\"depot.js?c7cd33e16bbca7cc5cc07860e98cd239\",\"autosize\":\"autosize.js?9fde45e4aa60d3d2c4a2d105df42ec6c\",\"talks\":\"talks.js?06a797089a6ca6e419ec70901d4c739d\",\"Cookies\":\"Cookies.js?3dd60a67a7315e345e3c63b9791e8488\",\"embed\":\"embed.js?83dd820ff14edf77928c9b8fa985423f\",\"settings\":\"settings.js?78af5f51b67f3044484a51b83e8846d0\",\"touchPunch\":\"touchPunch.js?09f0267b1ecb9ecae2dcc9c959aebcf8\",\"moment\":\"moment.js?ca364b2a5594ce7fe239e92050224cf7\",\"dashboard_sessions\":\"dashboard_sessions.js?7279e20e100f4eba18821fac7c4b8a5b\",\"crushinator\":\"crushinator.js?78fa02fc2be00f61a95c0f3a713e7066\",\"react-tap-event-plugin\":\"react-tap-event-plugin.js?137988ded7131171e450d7de1df7f8ab\",\"hbs/handlebars\":\"libs.js?98e58d8a8b878fe6bb21529b7b2f4a9d\"}};\n" +
                "</script>\n" +
                "<script>\n" +
                "  TED.abby.overridden = false;\n" +
                "  TED.abby.disabled = false;\n" +
                "  \n" +
                "  TED[\"controller\"]=\"pages\"\n" +
                "</script>\n" +
                "<script async=\"async\" data-main=\"pages\" src=\"https://pa.tedcdn.com/javascripts/core.js?1480618736\"></script></head>" +
                "\n" +
                "<body>\n" +
                "<noscript>\n" +
                "<div class='alert alert--flash alert--warning'>\n" +
                "<div class='container'>\n" +
                "<div class='h9'>You have JavaScript disabled</div>\n" +
                "For the best experience, please turn JavaScript on.\n" +
                "<a href='http://enable-javascript.com/'>Here's how</a>\n" +
                "</div>\n" +
                "</div>\n" +
                "</noscript>\n" +
                "<script>\n" +
                "  (function(d,h){\n" +
                "    if (('; '+d.cookie).match(/; _uconf=0;/)) {\n" +
                "      d.write(h);\n" +
                "      g('uconf.init',\"uconf\",\"uconf-close\");\n" +
                "    }\n" +
                "  }(document,\"\\u003cdiv class='alert alert--flash alert--warning' id='uconf'\\u003e\\n\\u003cdiv class='container'\\u003e\\n\\u003cdiv class='alert__container'\\u003e\\u003ch4 class='h10 m5'\\u003eYour account isn't active yet.\\u003c/h4\\u003ePlease click on the confirmation link we sent you.\\nIf you don't receive the email within ten minutes, we can\\n\\u003ca href='https://auth.ted.com/account/confirmation/new'\\u003esend it again\\u003c/a\\u003e.\\n\\u003ca class='alert__close g g-button-modal-close' href='#' id='uconf-close'\\u003eClose\\u003c/a\\u003e\\n\\u003c/div\\u003e\\n\\u003c/div\\u003e\\n\\u003c/div\\u003e\\n\"))\n" +
                "</script>" +
                "    <p>gus the the the the</p>\n<h1 class='h1 h1--alt m2'>\n" +
                "TED Fellows Program\n" +
                "</h1>\n" +
                "<!-- PB:B: copy --><div class='p2'>\n" +
                "<p>The TED Fellows program provides transformational support to a global network of 400 visionaries – scientists, artists, activists, entrepreneurs, doctors, journalists and inventors – who collaborate across disciplines to create positive change around the world.</p>\n" +
                "\n" +
                "</div>\n" +
                "\n" +
                "\n" +
                "</div>\n" +
                "\n" +
                "<div class='row'>\n" +
                "<div class='col col-lg-sidebarred'>\n" +
                "<!-- PB:B: heading --><h3 class=\"h4 m4\" id=\"h2--how-it-works\">How it works\n" +
                "</h3>\n" +
                "\n" +
                "<!-- PB:B: copy --><div class='copy text-left'>\n" +
                "<p>Every year, through a rigorous open application process, TED selects a group of rising stars to be TED Fellows. We choose Fellows based on remarkable achievement, their strength of character and on their innovative approach to solving the world’s tough problems. Fellows are invited to attend a TED conference, where they meet, exchange ideas and connect with the larger TED community. They also give their own TED Talk – an unprecedented opportunity to disseminate their unique ideas to the world.</p>\n" +
                "\n" +
                "<p><strong><a href=\"/participate/ted-fellows-program/apply-to-be-a-ted-fellow\">Apply to be a TED Fellow</a></strong></p>\n" +
                "\n" +
                "</div>" +
                "</body>\n" +
                "</html>").text();

        Document test = Jsoup.parse(documenttext.toString());
        if (test == null){
            return;
        }
        test.select("script,jscript,style").remove();
        String document;
        if (test.body() != null) {
            document = test.body().text();
        } else {
            return;
        }
        if (test.head() != null) {
            document += test.head().text();
        }

        String[] words = document.toLowerCase().split("[^\\p{Alnum}]+");
        //TODO: If running too slow, might want to get rid of stemming or extract to another for loop to stem only once
        Map<String, Integer> tfs = new HashMap<>();
        String stemmedWord;
        for (String w : words){
            stemmer.add(w.toCharArray(), w.length());
            stemmer.stem();

            stemmedWord = stemmer.toString();
            //System.out.println(stemmedWord);
            if (tfs.containsKey(stemmedWord)){
                tfs.put(stemmedWord, tfs.get(stemmedWord) + 1);
            } else {
                tfs.put(stemmedWord, 1);
            }
        }

        //find max
        Set<String> stopWords = new HashSet<>();
        stopWords.add("the");

        int max = -1;
        for (Map.Entry<String, Integer> e : tfs.entrySet()){

            System.out.println("WORD: " + e.getKey() + ", " + "NUM: " + e.getValue());
            if (e.getValue() > max){
                max = e.getValue();
            }
        }
        System.out.println(max);

        System.out.println();
        System.out.println();


        double idf = Math.log10((double)2/(double)1);




        for (String w : tfs.keySet()){
            if(!stopWords.contains(w)) {
                double tf2 = .5 + (.5 * (double) tfs.get(w) / max);
                System.out.println("WORD: " + w + ", " + "NUM: " + tf2 * idf);
            }
        }
    }

}
