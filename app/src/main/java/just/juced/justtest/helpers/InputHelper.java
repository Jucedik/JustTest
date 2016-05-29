package just.juced.justtest.helpers;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

/**
 * Created by juced on 09.11.2015.
 */
public class InputHelper {

    public static void hideKeyboard(Activity activity, View rootView) {
        View focusView =  activity.getCurrentFocus();
        if (focusView != null) {
            InputMethodManager inputManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(rootView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

}
