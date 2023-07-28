package com.example.chatwfapplication;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


public class GroupsFragment extends Fragment {

    private View groupFragmentView;

    private ListView list_view;
    private ArrayAdapter<String> arrayAdapter;
    private ArrayList<String> list_of_groups=new ArrayList<>();

    public GroupsFragment(){}

    private DatabaseReference GroupRef;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        groupFragmentView= inflater.inflate(R.layout.fragment_groups, container, false);
        //LayoutInflater will instantiate the xml file to be used in java file
        //ViewGroup is the parent viewgroup that contains the fragment
        //Bundle if true then fragment will resume from previous saved state else it will start from beginning

        GroupRef= FirebaseDatabase.getInstance().getReference().child("Groups");

        InitializeFields();

        RetrieveAndDisplayGroups();

        list_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                String currentGroupName=adapterView.getItemAtPosition(position).toString();

                //getContext() is used if we are currently on a fragment and
                // activity_name.this will be used if we are currently on an activity
                Intent groupChatIntent=new Intent(getContext(),GroupChatActivity.class);
                groupChatIntent.putExtra("groupName",currentGroupName);
                startActivity(groupChatIntent);


            }
        });

        return groupFragmentView;
    }

    private void RetrieveAndDisplayGroups() {
        GroupRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                Set<String> set=new HashSet<>();
                Iterator iterator=snapshot.getChildren().iterator();
                while(iterator.hasNext())
                {
                    set.add(((DataSnapshot)iterator.next()).getKey());
                }

                list_of_groups.clear();
                list_of_groups.addAll(set);
                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void InitializeFields() {
        list_view =(ListView) groupFragmentView.findViewById(R.id.list_view);
        arrayAdapter=new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, list_of_groups);
        list_view.setAdapter(arrayAdapter);
    }
}