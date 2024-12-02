package candybar.lib.fragments.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import candybar.lib.R;
import candybar.lib.preferences.Preferences;

public class PrivacyPolicyDialog extends DialogFragment {

    private static final String TAG = "privacy_policy_dialog";

    public static void show(FragmentActivity activity) {
        if (activity == null || activity.isFinishing()) {
            return;
        }

        Preferences preferences = Preferences.get(activity);
        // Show dialog if privacy policy not accepted OR if intro has been reset
        if (preferences.isPrivacyPolicyAccepted() && !preferences.isIntroReset()) {
            return;
        }

        // Reset privacy policy acceptance when intro is reset
        if (preferences.isIntroReset()) {
            preferences.setPrivacyPolicyAccepted(false);
            preferences.setIntroReset(false);
        }

        FragmentManager fm = activity.getSupportFragmentManager();
        Fragment fragment = fm.findFragmentByTag(TAG);
        if (fragment == null) {
            fragment = new PrivacyPolicyDialog();
            fm.beginTransaction()
                    .add(fragment, TAG)
                    .commit();
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity())
                .inflate(R.layout.dialog_privacy_policy, null);

        WebView webView = view.findViewById(R.id.webview);
        Button btnAccept = view.findViewById(R.id.btn_accept);
        Button btnDeny = view.findViewById(R.id.btn_deny);

        // Get theme colors
        TypedValue typedValue = new TypedValue();
        requireContext().getTheme().resolveAttribute(android.R.attr.textColorPrimary, typedValue, true);
        int textColorPrimary = typedValue.data;

        requireContext().getTheme().resolveAttribute(android.R.attr.textColorSecondary, typedValue, true);
        int textColorSecondary = typedValue.data;

        requireContext().getTheme().resolveAttribute(R.attr.cb_colorAccent, typedValue, true);
        int accentColor = typedValue.data;

        // Convert colors to hex strings
        String textColorPrimaryHex = String.format("#%06X", (0xFFFFFF & textColorPrimary));
        String textColorSecondaryHex = String.format("#%06X", (0xFFFFFF & textColorSecondary));
        String accentColorHex = String.format("#%06X", (0xFFFFFF & accentColor));

        // Configure WebView
        webView.setBackgroundColor(0x00000000); // Transparent background
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(false);
        webView.getSettings().setBuiltInZoomControls(false);
        webView.getSettings().setDisplayZoomControls(false);
        webView.setVerticalScrollBarEnabled(false);

        // Set text styles based on app theme
        String css = String.format(
            "body { " +
            "   color: %s !important; " +
            "   font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, sans-serif !important; " +
            "   font-size: 14px !important; " +
            "   line-height: 1.5 !important; " +
            "} " +
            "h1, h2 { " +
            "   color: %s !important; " +
            "   font-size: 18px !important; " +
            "   font-weight: 500 !important; " +
            "   margin-top: 16px !important; " +
            "   margin-bottom: 8px !important; " +
            "} " +
            "h3, h4, h5, h6 { " +
            "   color: %s !important; " +
            "   font-size: 16px !important; " +
            "   font-weight: 500 !important; " +
            "   margin-top: 12px !important; " +
            "   margin-bottom: 8px !important; " +
            "} " +
            "p, li { " +
            "   color: %s !important; " +
            "   font-size: 14px !important; " +
            "   line-height: 1.5 !important; " +
            "   margin-bottom: 8px !important; " +
            "} " +
            "strong { " +
            "   color: %s !important; " +
            "   font-weight: 500 !important; " +
            "} " +
            "a { " +
            "   color: %s !important; " +
            "   text-decoration: none !important; " +
            "} ",
            textColorSecondaryHex, // body text
            textColorPrimaryHex,   // h1, h2
            textColorPrimaryHex,   // h3-h6
            textColorSecondaryHex, // p, li
            textColorPrimaryHex,   // strong
            accentColorHex         // links
        );

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                String js = String.format(
                    "document.documentElement.style.setProperty('--text-color', '%s');" +
                    "document.documentElement.style.setProperty('--accent-color', '%s');" +
                    "document.body.style.padding = '0px';" +
                    "document.body.style.margin = '0px';" +
                    "var style = document.createElement('style');" +
                    "style.type = 'text/css';" +
                    "style.innerHTML = '%s';" +
                    "document.head.appendChild(style);",
                    textColorSecondaryHex,
                    accentColorHex,
                    css.replace("'", "\\'")
                );
                webView.evaluateJavascript(js, null);
            }
        });

        // Load the privacy policy HTML
        webView.loadUrl("file:///android_asset/privacy_policy.html");

        btnAccept.setOnClickListener(v -> {
            Preferences.get(requireContext()).setPrivacyPolicyAccepted(true);
            dismiss();
        });
        
        btnDeny.setOnClickListener(v -> {
            requireActivity().finishAffinity();
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setView(view);

        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(
                    new ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }

        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
        }
    }
}
