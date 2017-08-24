package com.qualityunit.android.liveagentphone.ui.home.contacts;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.qualityunit.android.liveagentphone.net.loader.PaginationList;
import com.qualityunit.android.liveagentphone.net.rest.ApiException;
import com.qualityunit.android.liveagentphone.net.rest.Client;
import com.qualityunit.android.liveagentphone.ui.common.BaseFragment;
import com.qualityunit.android.liveagentphone.ui.home.HomeActivity;
import com.qualityunit.android.liveagentphone.util.Logger;
import com.qualityunit.android.liveagentphone.util.Tools;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Retained fragment for handling contact list
 * Created by rasto on 28.11.16.
 */
public class ContactsRetainFragment extends BaseFragment<HomeActivity> {

    public static String TAG = ContactsRetainFragment.class.getSimpleName();
    private ContactPaginationList cpl = new ContactPaginationList();
    private String basePath;
    private String token;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    // ******** CPL API ************

    public void init (PaginationList.CallbackListener<ContactItem> callbackListener, String basePath, String token, boolean reinit) {
        this.basePath = basePath;
        this.token = token;
        cpl.setListener(callbackListener);
        cpl.init(createArgs(null), reinit);
    }

    /**
     * Avoid to call any callback method
     */
    public void stop () {
        cpl.setListener(null);
    }

    public void refresh () {
        cpl.refresh(null);
    }

    public void nextPage () {
        cpl.nextPage();
    }

    public void search (String searchTerm) {
        cpl.init(createArgs(searchTerm), true);
    }

    public void clear () {
        cpl.clear();
    }

    // ************ private methods ************

    private Bundle createArgs(@Nullable String searchTerm) {
        Bundle bundle = new Bundle();
        if (!TextUtils.isEmpty(searchTerm)) {
            bundle.putString("searchTerm", searchTerm);
        }
        bundle.putString("basePath", basePath);
        bundle.putString("token", token);
        bundle.putString("sortDirection", ContactFragment.SORT_DIRECTION);
        bundle.putString("sortField", ContactFragment.SORT_FIELD);
        return bundle;
    }

    // ********** INNER CLASSES ************

    private class ContactPaginationList extends PaginationList<ContactItem> {

        private final String TAG = ContactPaginationList.class.getSimpleName();

        public ContactPaginationList() {
            super(ContactFragment.FIRST_PAGE, ContactFragment.ITEMS_PER_PAGE);
        }

        @Override
        public List<ContactItem> loadList(int pageToLoad, Bundle args) throws Exception {
            JSONObject filters = new JSONObject();
            filters.put("type", "V"); // default
            filters.put("hasPhone", "Y"); // default
            if (args.containsKey("searchTerm")) {
                filters.put("q", args.getString("searchTerm"));
            }
            String filtersEncoded = URLEncoder.encode(filters.toString(), "utf-8");
            String basePath = stringFromArg(args, "basePath");
            String token = stringFromArg(args, "token");
            String sortDirection = stringFromArg(args, "sortDirection");
            String sortField = stringFromArg(args, "sortField");
            return getContactsList(basePath, token, sortDirection, sortField, filtersEncoded, pageToLoad, ContactFragment.ITEMS_PER_PAGE);
        }

        private String stringFromArg(Bundle args, String string) throws Exception {
            if (!args.containsKey(string)) {
                throw new Exception("Missing '" + string + "'!");
            }
            return args.getString(string);
        }

        private List<ContactItem> getContactsList(String basePath, String token, String sortDirection, String sortField, String filtersEncoded, int requestedPage, int itemsPerPage) throws IOException, ApiException {
            List<ContactItem> list = new ArrayList<>();
            Client client = Client.getInstance();
            Request request = client.GET(basePath, "/contacts", token)
                    .addParam("_perPage", itemsPerPage)
                    .addParam("_page", requestedPage)
                    .addParam("_sortField", sortField)
                    .addParam("_sortDir", sortDirection)
                    .addEncodedParam("_filters", filtersEncoded)
                    .build();
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                if (response.body() == null) {
                    Logger.e(TAG, Tools.formatError("Missing response body"));
                    return list;
                }
                try {
                    JSONArray array = new JSONArray(response.body().string());
//                    JSONArray array = new JSONArray("dsadasdasdasds");
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject jsonItem = array.getJSONObject(i);
                        ContactItem contactItem = new ContactItem(
                                Tools.getStringFromJson(jsonItem, "id"),
                                Tools.getStringFromJson(jsonItem, "firstname"),
                                Tools.getStringFromJson(jsonItem, "lastname"),
                                Tools.getStringFromJson(jsonItem, "system_name"),
                                Tools.getStringFromJson(jsonItem, "avatar_url"),
                                Tools.getStringFromJson(jsonItem, "type")
                        );
                        contactItem.emails = Tools.getStringArrayFromJson(jsonItem, "emails");
                        contactItem.phones = Tools.getStringArrayFromJson(jsonItem, "phones");
                        list.add(contactItem);
                    }

                } catch (IOException | JSONException e) {
                    Logger.e(TAG, e.getMessage());
                }
            }
            else {
                throw new ApiException(response.message(), response.code());
            }
            return list;
        }

    }
}
