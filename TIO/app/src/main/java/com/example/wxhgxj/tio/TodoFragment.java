package com.example.wxhgxj.tio;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;


/**
 * A simple {@link Fragment} subclass.
 */
public class TodoFragment extends Fragment {

    public TodoFragment() {
        // Required empty public constructor
    }

    private View todoView;
    private RecyclerView todoList;
    private DatabaseReference todoRef;
    private FirebaseRecyclerAdapter<TodoEvent, TodoViewHolder> firebaseRecyclerAdapter;
    private Button addTodo;
    private Button cancelTodo;
    private EditText newTodoInput;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        todoView = inflater.inflate(R.layout.fragment_todo, container, false);
        todoList = (RecyclerView)todoView.findViewById(R.id.todoList);
        String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        todoRef = FirebaseDatabase.getInstance().getReference("Todo").child(currentUid);
        addTodo = (Button)todoView.findViewById(R.id.addTodoButton);
        cancelTodo = (Button)todoView.findViewById(R.id.cancelTodoButton);
        newTodoInput = (EditText)todoView.findViewById(R.id.newTodoEventInput);
        todoList.setHasFixedSize(true);
        todoList.setLayoutManager(new LinearLayoutManager(getContext()));
        Query query = todoRef;
        FirebaseRecyclerOptions<TodoEvent> options = new FirebaseRecyclerOptions.Builder<TodoEvent>()
                .setQuery(query, TodoEvent.class)
                .build();
        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<TodoEvent, TodoViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final TodoViewHolder holder, final int position, @NonNull TodoEvent model) {
                final String currentTodoID = getRef(position).getKey();
                final String content = model.getContent();
                holder.setContent(content);
                Button editTodo = holder.getEditButton();
                Button doneTodo = holder.getDoneButton();
                final EditText newContent = holder.getNewTodoContent();
                final Button cancelButton = holder.getCancelButton();
                final Button updateButton = holder.getUpdateButton();
                editTodo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        newContent.setText(content);
                        holder.showUpdate();
                        holder.hideDisplay();
                    }
                });
                doneTodo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        getRef(position).removeValue();
                        firebaseRecyclerAdapter.notifyItemRemoved(position);
                        firebaseRecyclerAdapter.notifyItemRangeChanged(position, getItemCount());
                        firebaseRecyclerAdapter.notifyDataSetChanged();
                    }
                });
                updateButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String newTodoContent = newContent.getText().toString();
                        todoRef.child(currentTodoID).child("Content").setValue(newTodoContent);
                        holder.showDisplay();
                        holder.hideUpdate();
                    }
                });
                cancelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        holder.hideUpdate();
                        holder.showDisplay();
                    }
                });
            }
            @NonNull
            @Override
            public TodoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.single_todo_event, parent, false);
                return new TodoViewHolder(view);
            }
        };
        todoList.setAdapter(firebaseRecyclerAdapter);
        addTodo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = addTodo.getText().toString();
                if(text.equals("Add New Todo Event")) {
                    newTodoInput.setVisibility(View.VISIBLE);
                    cancelTodo.setVisibility(View.VISIBLE);
                    addTodo.setText("Confirm");
                } else {
                    String content = newTodoInput.getText().toString();
                    todoRef.push().child("Content").setValue(content);
                    newTodoInput.setVisibility(View.GONE);
                    cancelTodo.setVisibility(View.GONE);
                    addTodo.setText("Add New Todo Event");
                }
            }
        });
        cancelTodo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                newTodoInput.setVisibility(View.GONE);
                cancelTodo.setVisibility(View.GONE);
                addTodo.setText("Add New Todo Event");
            }
        });
        return todoView;
    }

    @Override
    public void onStart() {
        super.onStart();
        firebaseRecyclerAdapter.startListening();
    }

}
