package nl.mprog.todoloo;

import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {

    ArrayList<String> tasks = new ArrayList<>();
    ArrayAdapter<String> tasksAdapter;
    boolean noTasks = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            readTasks();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id){
            case R.id.action_clear:
                removeAllTasks();
                return true;
            case R.id.action_quit:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    /**
     * Calls writeTasks() whenever the Activity reaches the onStop phase.
     */
    @Override
    public void onStop(){
        super.onStop();
        removeDummyTask();
        writeTasks();

    }

    /**
     *  Tries to Read tasks from the file 'tasks' and initializes the List from it.
     *  Each line in the file corresponds to one task in the list.
     *  If the file is empty readTasks() provides a dummy task called "Add a new task"
     */

    public void readTasks() throws IOException {
        Scanner scan;

        //For each line in the file 'tasks' add the line to tasks ArrayList
        try {
            scan = new Scanner(openFileInput("tasks"));
            while (scan.hasNextLine()) {
                String line = scan.nextLine();
                tasks.add(line);
            }
        } catch (FileNotFoundException e) {
            addDummyTask();
        }

        addDummyTask();
        Log.v("tasks", tasks.toString());

        //Set the ListView adapter, which will make a list item for each task
        tasksAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, tasks);
        ListView tasksList = (ListView) findViewById(R.id.list);
        tasksList.setAdapter(tasksAdapter);
        tasksAdapter.notifyDataSetChanged();

        //Set the ListView OnItemLongClickListener, will call removeTask on every long click
        tasksList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                TextView taskView = (TextView) view;
                String task = String.valueOf(taskView.getText());
                if (removeTask(task)) {
                    tasksAdapter.notifyDataSetChanged();
                    return true;
                } else {
                    return false;
                }
            }
        });


    }

    /**
     * Adds a dummy task "Add a new task" to the tasks list when called
     * And sets the boolean noTasks to True.
     */
    public void addDummyTask(){
        if(tasks.isEmpty()) {
            tasks.add(0, getString(R.string.no_tasks));
            noTasks = true;
        }
    }

    /**
     * Removes the dummy "Add a new task" from the task list when called
     * And sets the boolean noTasks to False
     */
    public void removeDummyTask(){
        if (noTasks) {
            tasks.remove(tasks.remove(tasks.indexOf(getString(R.string.no_tasks))));
            noTasks = false;
        }
    }

    /**
     * Adds an item to the tasklist and refreshes the List- and EditText views
     * Will also remove the dummy task 'Add a new task' if it was present.
     * @param view floating action button, not actually used.
     */

    public void addTask(View view) {

        //Get task from EditText view, and remove all newlines
        //This is necessary to properly save each task,
        // otherwise it'd be read as two separate tasks on restart.
        EditText taskView = (EditText) findViewById(R.id.newtask_text);
        String newtask = taskView.getText().toString();
        newtask = newtask.replaceAll("\n", " ");
        //Log.v("newtask", newtask);


        //if the new task is an empty string, show a message to the user and don't add anything
        //to the task list. Otherwise, just add it to the task list
        if(newtask.equals("")) {
            Snackbar.make(taskView, R.string.empty_task, Snackbar.LENGTH_SHORT).show();
        }
        else {
            tasks.add(newtask);

            //if there were no tasks present, remove the dummy task and
            //update noTask boolean.
            removeDummyTask();

            //Update listView and reset EditText
            tasksAdapter.notifyDataSetChanged();
            taskView.setText("");
        }
    }

    /**
     * Removes a task from the task list
     * If no tasks remain after the current task is removed,
     * then it adds the dummy task 'Add a new task'
     * @param task item to delete
     * @return true if the task was in the list and is now removed, false if otherwise.
     */
    public boolean removeTask(String task) {
        //if task is in tasklist, remove it.
        if(tasks.contains(task)) {
            tasks.remove(tasks.indexOf(task));
            //Log.v("tasks", "removed task: " + task)

            //If no task remains now, re-add the dummy task.
            addDummyTask();

            return true;
        }
        else {
            return false;
        }
    }

    public void removeAllTasks() {
        tasks.clear();
        addDummyTask();
        tasksAdapter.notifyDataSetChanged();
    }

    /**
     * Tries to write the task list to the file 'tasks', one task per line.
     *
     */
    public void writeTasks() {

        PrintStream out;
        try{
            //open file
            out = new PrintStream(openFileOutput("tasks", MODE_PRIVATE));

            //Write each task to a new line in the file
            for(int i=0;i<tasks.size();i++) {
                out.println(tasks.get(i));
                //Log.v("tasks", "Wrote " + tasks.get(i));
            }
            //finally close file
            out.close();
            Log.v("tasks", "saved tasks");

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


    }

}
