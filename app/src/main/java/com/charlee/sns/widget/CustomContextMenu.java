package com.charlee.sns.widget;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.charlee.sns.R;

/**
 */
public class CustomContextMenu extends Dialog implements View.OnClickListener {
    private Context context;
    private LinearLayout customMenu;
    private LayoutInflater inflater;
    private OnItemSelectedListener onItemSelectedListener;

    public static class ItemStyle {
        public static final int NONE = 0x00;
        public static final int DIVIDER_TOP = 0x01;
        public static final int DIVIDER_BOTTOM = 0x02;
        public static final int SPACE_TOP = 0x04;
        public static final int SPACE_BOTTOM = 0x08;
    }

    public CustomContextMenu(Context context) {
        super(context, R.style.custom_menu_style);
        init(context);
    }

    private void init(Context context) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        customMenu = (LinearLayout) inflater.inflate(R.layout.custom_menu, null);
        this.setContentView(customMenu);
    }

    public void add(int itemStyle, final int itemId, final int itemTextResId) {
        add(itemStyle, itemId, itemTextResId,
                R.drawable.selector_custom_menu_text_color,
                R.drawable.selector_custom_menu_text_bg);
    }

    public void add(int itemStyle, final int itemId,
                    final int itemTextId,
                    final int itemTextColorId,
                    final int itemTextBgId) {
        View itemView = inflater.inflate(R.layout.custom_menu_item, null);
        itemView.setId(itemId);
        TextView textView = (TextView) itemView.findViewById(R.id.txt_menu_item);
        textView.setText(itemTextId);
        textView.setTextColor(context.getResources().getColorStateList(itemTextColorId));
        textView.setBackgroundResource(itemTextBgId);
        textView.setTag(itemView);
        View dividerTop = itemView.findViewById(R.id.divider_top);
        View dividerBottom = itemView.findViewById(R.id.divider_bottom);
        View spaceTop = itemView.findViewById(R.id.space_top);
        View spaceBottom = itemView.findViewById(R.id.space_bottom);

        // 处理style
        dividerTop.setVisibility((itemStyle & ItemStyle.DIVIDER_TOP) == ItemStyle.DIVIDER_TOP
                ? View.VISIBLE : View.GONE);
        dividerBottom.setVisibility((itemStyle & ItemStyle.DIVIDER_BOTTOM) == ItemStyle.DIVIDER_BOTTOM
                ? View.VISIBLE : View.GONE);
        spaceTop.setVisibility((itemStyle & ItemStyle.SPACE_TOP) == ItemStyle.SPACE_TOP
                ? View.VISIBLE : View.GONE);
        spaceBottom.setVisibility((itemStyle & ItemStyle.SPACE_BOTTOM) == ItemStyle.SPACE_BOTTOM
                ? View.VISIBLE : View.GONE);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        customMenu.addView(itemView, lp);
        textView.setOnClickListener(this);
    }

    public void setMenuItemEnable(final int itemId, boolean enable) {
        View itemView = customMenu.findViewById(itemId);
        if (itemView != null) {
            itemView.setEnabled(enable);
            TextView textView = (TextView) itemView.findViewById(R.id.txt_menu_item);
            textView.setEnabled(enable);
        }
    }

    public void setOnItemSelectedListener(OnItemSelectedListener listener) {
        onItemSelectedListener = listener;
    }

    @Override
    public void onClick(View v) {
        Object tag = v.getTag();
        if (!(tag instanceof View)) {
            return;
        }

        int itemId = ((View) tag).getId();
        if (onItemSelectedListener != null) {
            onItemSelectedListener.onItemSelected(itemId, (View) tag);
        }
        if (this.isShowing()) {
            this.dismiss();
        }
    }

    public interface OnItemSelectedListener {
        void onItemSelected(int itemId, View itemView);
    }

    @Override
    public void show() {
        Window dialogWindow = this.getWindow();
        dialogWindow.setGravity(Gravity.CENTER);
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();

        if (context instanceof  Activity) {
            WindowManager wm = ((Activity) context).getWindowManager();
            Display d = wm.getDefaultDisplay();
            lp.width = (int) (d.getWidth() * 0.8);
            dialogWindow.setAttributes(lp);
        } else {
            lp.width = context.getResources().getDimensionPixelSize(R.dimen.custom_menu_width);
            dialogWindow.setAttributes(lp);
        }

        super.show();
    }
}
