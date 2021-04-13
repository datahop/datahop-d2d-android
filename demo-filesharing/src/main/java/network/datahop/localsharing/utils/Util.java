package network.datahop.localsharing.utils;

import android.content.Context;
import android.os.Build;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Util {


    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.toLowerCase().startsWith(manufacturer.toLowerCase())) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }



    private static String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }

    //Current Android version data
    public static String currentVersion(){
        double release=Double.parseDouble(Build.VERSION.RELEASE.replaceAll("(\\d+[.]\\d+)(.*)","$1"));
        String codeName="Unsupported";//below Jelly bean OR above Oreo
        if(release>=4.1 && release<4.4)codeName="Jelly Bean";
        else if(release<5)codeName="Kit Kat";
        else if(release<6)codeName="Lollipop";
        else if(release<7)codeName="Marshmallow";
        else if(release<8)codeName="Nougat";
        else if(release<9)codeName="Oreo";
        return codeName+" v"+release+", API Level: "+Build.VERSION.SDK_INT;
    }



    public static String localizeNumber(Context context, long number) {
        NumberFormat nf = NumberFormat.getInstance();
        return nf.format(number);
    }

    private static String formatDate(Context context, String date) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date datum = null;
        try {
            datum = formatter.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }


        DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM);

        return df.format(datum);
    }

    /*public static String localizeDate(Context context, String date) {
        Resources res = context.getResources();
        String dateString = res.getString(R.string.upload_date_text);

        String formattedDate = formatDate(context, date);
        return String.format(dateString, formattedDate);
    }

    public static String localizeViewCount(Context context, long viewCount) {
        return getQuantity(context, R.plurals.views, R.string.no_views, viewCount, localizeNumber(context, viewCount));
    }

    public static String localizeSubscribersCount(Context context, long subscriberCount) {
        return getQuantity(context, R.plurals.subscribers, R.string.no_subscribers, subscriberCount, localizeNumber(context, subscriberCount));
    }

    public static String localizeStreamCount(Context context, long streamCount) {
        return getQuantity(context, R.plurals.videos, R.string.no_videos, streamCount, localizeNumber(context, streamCount));
    }

    public static String shortCount(Context context, long count) {
        if (count >= 1000000000) {
            return Long.toString(count / 1000000000) + context.getString(R.string.short_billion);
        } else if (count >= 1000000) {
            return Long.toString(count / 1000000) + context.getString(R.string.short_million);
        } else if (count >= 1000) {
            return Long.toString(count / 1000) + context.getString(R.string.short_thousand);
        } else {
            return Long.toString(count);
        }
    }

    public static String shortViewCount(Context context, long viewCount) {
        return getQuantity(context, R.plurals.views, R.string.no_views, viewCount, shortCount(context, viewCount));
    }

    public static String shortSubscriberCount(Context context, long subscriberCount) {
        return getQuantity(context, R.plurals.subscribers, R.string.no_subscribers, subscriberCount, shortCount(context, subscriberCount));
    }

    private static String getQuantity(Context context, @PluralsRes int pluralId, @StringRes int zeroCaseStringId, long count, String formattedCount) {
        if (count == 0) return context.getString(zeroCaseStringId);

        // As we use the already formatted count, is not the responsibility of this method handle long numbers
        // (it probably will fall in the "other" category, or some language have some specific rule... then we have to change it)
        int safeCount = count > Integer.MAX_VALUE ? Integer.MAX_VALUE : count < Integer.MIN_VALUE ? Integer.MIN_VALUE : (int) count;
        return context.getResources().getQuantityString(pluralId, safeCount, formattedCount);
    }

    public static String getDurationString(long duration) {
        if (duration < 0) {
            duration = 0;
        }
        String output;
        long days = duration / (24 * 60 * 60L); // greater than a day
        duration %= (24 * 60 * 60L);
        long hours = duration / (60 * 60L); // greater than an hour
        duration %= (60 * 60L);
        long minutes = duration / 60L;
        long seconds = duration % 60L;

        //handle days
        if (days > 0) {
            output = String.format(Locale.US, "%d:%02d:%02d:%02d", days, hours, minutes, seconds);
        } else if (hours > 0) {
            output = String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds);
        } else {
            output = String.format(Locale.US, "%d:%02d", minutes, seconds);
        }
        return output;
    }*/

}
