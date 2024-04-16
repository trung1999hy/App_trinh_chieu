package com.thn.videoconstruction.fe_ui.inapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.thn.videoconstruction.R;
import com.thn.videoconstruction.utils.Constants;
import com.android.billingclient.api.ProductDetails;

import java.util.ArrayList;
import java.util.List;

public class PurchaseInAppAdapter extends RecyclerView.Adapter<PurchaseInAppAdapter.ViewHolder> {

    private OnClickListener onClickListener;
    private List<ProductDetails> productDetailsList = new ArrayList<>();
    private Context context;

    public void setData(Context context, List<ProductDetails> productDetailsList) {
        this.productDetailsList = productDetailsList;
        this.context = context;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View rootView = LayoutInflater.from(context).inflate(R.layout.item_subscription, parent, false);
        return new ViewHolder(rootView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.display(productDetailsList.get(position));
    }

    @Override
    public int getItemCount() {
        return productDetailsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView tvSubName;
        private ConstraintLayout item;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSubName = itemView.findViewById(R.id.tvSubName);
            item = itemView.findViewById(R.id.item);
        }

        public void display(final ProductDetails productDetails) {
            if (productDetails == null) return;
            String price = productDetails.getOneTimePurchaseOfferDetails().getFormattedPrice();
            tvSubName.setText(setTitleValue(productDetails.getProductId(), price));
            item.setOnClickListener(view -> onClickListener.onClickItem(productDetails));
        }
    }

    public void setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public interface OnClickListener {
        void onClickItem(ProductDetails item);
    }

    private String setTitleValue(String productId, String price) {
        switch (productId) {
            case Constants.KEY_10_COIN:
                return String.format(context.getResources().getString(R.string.message_purchase_one), price + "/50 gold");
            case Constants.KEY_20_COIN:
                return String.format(context.getResources().getString(R.string.message_purchase_one), price + "/150 gold");
            case Constants.KEY_50_COIN:
                return String.format(context.getResources().getString(R.string.message_purchase_one), price + "/300 gold");
            case Constants.KEY_100_COIN:
                return String.format(context.getResources().getString(R.string.message_purchase_one), price + "/500 gold");
            case Constants.KEY_150_COIN:
                return String.format(context.getResources().getString(R.string.message_purchase_one), price + "/700 gold");
            case Constants.KEY_200_COIN:
                return String.format(context.getResources().getString(R.string.message_purchase_one), price + "/999 gold");
            case Constants.KEY_5_COIN:
                return String.format(context.getResources().getString(R.string.message_purchase_one), price + "/25 gold");
            case Constants.KEY_60_COIN:
                return String.format(context.getResources().getString(R.string.message_purchase_one), price + "/600 gold");

            default:
                return String.format(context.getResources().getString(R.string.message_purchase_one), "/0 gold");
        }
    }
}
