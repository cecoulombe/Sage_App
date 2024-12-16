package com.example.financemanagerapp;

import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;

import android.text.Editable;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import android.widget.ArrayAdapter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CurrencyExchangeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CurrencyExchangeFragment extends DialogFragment {

    private EditText inputAmount, outputAmount;
    private Spinner startSpinner, endSpinner;
    private boolean isUpdating = false;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public CurrencyExchangeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment currencyExchangeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CurrencyExchangeFragment newInstance(String param1, String param2) {
        CurrencyExchangeFragment fragment = new CurrencyExchangeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_currency_exchange, container, false);

        // Initialize views
        inputAmount = view.findViewById(R.id.inputAmount);
        outputAmount = view.findViewById(R.id.outputAmount);
        startSpinner = view.findViewById(R.id.startCurrencySpinner);
        endSpinner = view.findViewById(R.id.endCurrencySpinner);


        // Set up the exit button listener
        ImageButton exitButton = view.findViewById(R.id.closeButton); // Reference to the close button
        exitButton.setOnClickListener(v -> dismiss());

        // populate spinners
        try {
            populateSpinner(startSpinner, "input");
            populateSpinner(endSpinner, "output");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // add a text watcher for both input and output amounts
        inputAmount.addTextChangedListener(new CurrencyTextWatcher(true));
        outputAmount.addTextChangedListener(new CurrencyTextWatcher(false));

        return view;
    }

    // populates the spinners based on the currencies available
    private void populateSpinner(Spinner spinner, String type) throws IOException {
        // Read JSON file from raw folder
        InputStream inputStream = getResources().openRawResource(R.raw.currencies);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder stringBuilder = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line);
        }
        String json = stringBuilder.toString();

        // Parse the JSON data
        Gson gson = new Gson();
        Type listType = new TypeToken<List<Currency>>() {}.getType();
        List<Currency> currencies = gson.fromJson(json, listType);

        // Set up the spinner adapter
        ArrayAdapter<Currency> adapter = new ArrayAdapter<>(
                getActivity(), android.R.layout.simple_spinner_item, currencies);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        // add listeners for the spinners
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                Currency selectedCurrency = (Currency) parentView.getItemAtPosition(position);
                String currencyCode = selectedCurrency.getCode();

                // Trigger the conversion when the spinner item is selected
                convertAmount(true, inputAmount.getText().toString());  // Convert when the start currency is selected
                convertAmount(false, outputAmount.getText().toString());  // Convert when the end currency is selected
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing
            }
        });
    }

    // decides which currency is the start and which is the destination. Calls the API accordingly and makes the conversion
    private void convertAmount(boolean isInputSource, String amountStr) {
        if (amountStr.isEmpty()) return;

        try {
            double amount = Double.parseDouble(amountStr);
            String fromCurrency = isInputSource ? ((Currency) startSpinner.getSelectedItem()).getCode() : ((Currency) endSpinner.getSelectedItem()).getCode();
            String toCurrency = isInputSource ? ((Currency) endSpinner.getSelectedItem()).getCode() : ((Currency) startSpinner.getSelectedItem()).getCode();

            // Fetch exchange rate and calculate
            getExchangeRate(fromCurrency, toCurrency, amount, isInputSource);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    // gets the current exchange rates from the API
    private void getExchangeRate(String from, String to, double amount, boolean isInputSource) {
        String apiUrl = "https://v6.exchangerate-api.com/v6/26716265cb570afd57f0af33/latest/" + from;

        new Thread(() -> {
            try {
                // Make HTTP request and parse the response
                URL url = new URL(apiUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(10000); // Set timeout for connection
                connection.setReadTimeout(10000);    // Set timeout for reading data

                int responseCode = connection.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    throw new IOException("HTTP error code: " + responseCode);
                }

                InputStream inputStream = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

                // Log the raw response to check for potential issues
                Log.d("CurrencyExchange", "API Response: " + result.toString());

                JSONObject jsonObject = new JSONObject(result.toString());

                // Check if the "rates" field exists before attempting to use it
                if (jsonObject.has("conversion_rates") && !jsonObject.isNull("conversion_rates")) {
                    JSONObject rates = jsonObject.getJSONObject("conversion_rates");

                    // Check if the 'to' currency is available in the rates object
                    if (rates.has(to)) {
                        double rate = rates.getDouble(to);
                        double convertedAmount = amount * rate;

                        // Update UI on main thread
                        requireActivity().runOnUiThread(() -> {
                            isUpdating = true;
                            if (isInputSource) {
                                outputAmount.setText(String.format("%.2f", convertedAmount));
                            } else {
                                inputAmount.setText(String.format("%.2f", convertedAmount));
                            }
                            isUpdating = false;
                        });
                    } else {
                        // Handle case when 'to' currency is missing in the rates
                        requireActivity().runOnUiThread(() ->
                                Toast.makeText(getContext(), "Currency not found in the rates", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    // Handle case when 'conversion_rates' field is missing or null
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Rates data is missing from the response", Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "Error fetching exchange rate: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private class CurrencyTextWatcher implements TextWatcher {
        private final boolean isInputSource;

        public CurrencyTextWatcher(boolean isInputSource) {
            this.isInputSource = isInputSource;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable s) {
            if (isUpdating) return; // Avoid infinite loop
            convertAmount(isInputSource, s.toString());
        }
    }


    @Override
    public void onStart() {
        super.onStart();

        // Customize the dialog size
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
}