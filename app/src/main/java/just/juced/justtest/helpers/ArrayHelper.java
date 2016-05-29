package just.juced.justtest.helpers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by juced on 28.05.2016.
 */
public class ArrayHelper {

    public static boolean equalLists(List<String> a, List<String> b){
        // Check for sizes
        if ((a.size() != b.size())){
            return false;
        }

        return a.equals(b);
    }

}
