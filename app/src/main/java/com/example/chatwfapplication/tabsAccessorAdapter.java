package com.example.chatwfapplication;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.chatwfapplication.ChatsFragment;
import com.example.chatwfapplication.ContactsFragment;
import com.example.chatwfapplication.GroupsFragment;

public class tabsAccessorAdapter extends FragmentStateAdapter {


    private String[] titles={"Chats","Groups","Contacts","Requests"};

    public tabsAccessorAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch(position)
        {
            case 0:
                return new ChatsFragment();

            case 1:
                return new GroupsFragment();

            case 2:
                return new ContactsFragment();

            case 3:
                return new RequestFragment();
        }
        return null;
    }

    @Override
    public int getItemCount() {
        return titles.length;
    }
}
