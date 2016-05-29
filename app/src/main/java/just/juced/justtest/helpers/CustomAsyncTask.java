package just.juced.justtest.helpers;

import android.os.AsyncTask;

/**
 * Created by juced on 15.05.2016.
 */
public class CustomAsyncTask {

    public interface Interface_CustomAsyncTask {
        Object backgroundWork();
        void taskCompleted(Object... result);
    }

    private Interface_CustomAsyncTask interface_customAsyncTask;
    private Task_background task_background;

    public CustomAsyncTask(Interface_CustomAsyncTask interface_customAsyncTask) {
        this.interface_customAsyncTask = interface_customAsyncTask;
    }

    public void runTask() {
        task_background = new Task_background();
        task_background.execute();
    }

    /**
     * Background task
     */
    private class Task_background extends AsyncTask<Object, Integer, Object> {
        protected Object doInBackground(Object... params) {
            return interface_customAsyncTask.backgroundWork();
        }

        protected void onPostExecute(Object result) {
            postExecute(result);
        }
    }

    /**
     * Call this event after background task successfully completed
     * @param result
     */
    private void postExecute(Object result) {
        interface_customAsyncTask.taskCompleted(result);
    }

}
