package Adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import Fragments.ChatsFragment;
import Fragments.FriendsFragment;
import Fragments.RequestsFragment;

public class SectionsPageAdapter extends FragmentPagerAdapter {

    public SectionsPageAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {

        //Importing The Fragments In The Adapter.
        switch (position) {

            case 0:
                RequestsFragment requestsFragment = new RequestsFragment();
                return requestsFragment;

            case 1:
                ChatsFragment chatsFragment = new ChatsFragment();
                return chatsFragment;

            case 2:
                FriendsFragment friendsFragment = new FriendsFragment();
                return friendsFragment;

            default:
                return null;

        }
    }

    //Setting the No. Of Fragments.
    @Override
    public int getCount() {
        return 3;
    }

    //Setting a New InBuilt Method To Set the Titles of the Fragments.
    public CharSequence getPageTitle(int position){

        switch (position){

            case 0:
                return "REQUESTS";

            case 1:
                return "CHATS";

            case 2:
                return "FRIENDS";

            default:
                return null;

        }

    }

}
