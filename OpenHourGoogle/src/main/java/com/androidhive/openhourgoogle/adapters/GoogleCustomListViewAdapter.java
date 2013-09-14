package com.androidhive.openhourgoogle.adapters;

/**
 * Author: JessicaC
 * Date: 8/15/13
 *
 * This class is a custom list adapter class
 * for displaying each item in the list.
 */

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.androidhive.openhourgoogle.components.Place;
import com.androidhive.openhourgoogle.R;

import java.util.List;

public class GoogleCustomListViewAdapter extends ArrayAdapter<Place> {

    private static String PHOTO_SEARCH_URL = "https://maps.googleapis.com/maps/api/place/photo?";
    private static String maxwidth = "200";
    private static String maxheight = "200";
    private static String sensor = "false";

    Context context;
    ViewHolder holder;
    String imageUrl = null;

    /**
     * Constructor
     */
    public GoogleCustomListViewAdapter(Context context, int resourceId, List<Place> items) {
        super(context, resourceId, items);
        this.context = context;
    }

    /* private view holder class */
    private class ViewHolder {
        ImageView imageView;
        TextView txtTitle;
        TextView txtDesc;
        TextView txtOpen;
    }

    /**
     * @return the view of the list at specify position.
     */
    public View getView(int position, View convertView, ViewGroup parent) {
        holder = null;
        Place rowItem = getItem(position);

        LayoutInflater mInflater = (LayoutInflater) context
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.custom_list_item, null);
            holder = new ViewHolder();
            holder.txtDesc = (TextView) convertView.findViewById(R.id.tv_desc);
            holder.txtTitle = (TextView) convertView.findViewById(R.id.tv_title);
            holder.txtOpen = (TextView) convertView.findViewById(R.id.tv_open);
            holder.imageView = (ImageView) convertView.findViewById(R.id.iv_icon);
            convertView.setTag(holder);
        } else
            holder = (ViewHolder) convertView.getTag();

        holder.txtDesc.setText(rowItem.vicinity);
        holder.txtTitle.setText(rowItem.name);
        holder.imageView.setImageResource(R.drawable.ic_restaurant);
        if (rowItem.getOpenNow().equals("Open")) {
            holder.txtOpen.setTextColor(Color.parseColor("#00CC00"));
        } else {
            holder.txtOpen.setTextColor(Color.RED);
        }
        holder.txtOpen.setText(rowItem.getOpenNow());

          //TODO get photoref
//        String photoRef = (rowItem.photos == null || rowItem.photos.length < 1)?
//                null: rowItem.photos[0].photo_reference;
//        if (photoRef == null) {
//            holder.imageView.setImageResource(R.drawable.ic_restaurant);
//        } else {
//            imageUrl = PHOTO_SEARCH_URL + "maxwidth=" + maxwidth + "&maxheight" + maxheight
//                    + "&photoreference=" + photoRef + "&sensor=" + sensor +"&key=" + LocationUtils.GOOGLE_API_KEY;
//
//            ImageLoader.getInstance().displayImage(imageUrl, holder.imageView);
//        }

        return convertView;
    }

      /*
      * Download image url to bitmap, and set image to the image view.
      * /
//    private class DownloadImageTask extends AsyncTask<ImageView, Void, Bitmap> {
//
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//        }
//
//        @Override
//        protected Bitmap doInBackground(ImageView... imageView) {
//            //url to get the image
//            /*
//            String maxwidth = "200";
//            String maxheight = "200";
//            String sensor = "false";
//            String imageUrl = PHOTO_SEARCH_URL + "maxwidth=" + maxwidth + "&maxheight" + maxheight
//                    + "&photoreference=" + photoRef + "&sensor=" + sensor +"&key=" + API_KEY;
//            return downloadImage(imageUrl);
//            */
//
//            return downloadImage(imageUrl);
//        }
//
//        @Override
//        protected void onPostExecute(Bitmap bitmap) {
//            super.onPostExecute(bitmap);
//            if (bitmap != null) {
//                holder.imageView.setImageBitmap(bitmap);
//            } else {
//                holder.imageView.setImageResource(R.drawable.ic_restaurant);
//            }
//        }
//
//        /**
//         * Convert image url to bitmap
//         * @param imageUrl
//         * @return
//         */
//        private Bitmap downloadImage(String imageUrl) {
//            try {
//                return BitmapFactory.decodeStream((InputStream)new URL(imageUrl).getContent());
//
//            } catch (MalformedURLException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//            return null;
//        }
//    }
}