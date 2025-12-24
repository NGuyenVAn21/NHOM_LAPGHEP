package com.example.bookhub;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class CartManager {
    private static final String PREF_NAME = "cart_prefs";
    private static final String CART_ITEMS_KEY = "cart_items";
    private static CartManager instance;
    private SharedPreferences prefs;
    private Gson gson;
    private List<Book> cartItems;

    private CartManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
        loadCartItems();
    }

    public static synchronized CartManager getInstance(Context context) {
        if (instance == null) {
            instance = new CartManager(context);
        }
        return instance;
    }

    public void addToCart(Book book) {
        if (!isBookInCart(book.getId())) {
            cartItems.add(book);
            saveCartItems();
        }
    }

    public void removeFromCart(int bookId) {
        for (int i = 0; i < cartItems.size(); i++) {
            if (cartItems.get(i).getId() == bookId) {
                cartItems.remove(i);
                saveCartItems();
                break;
            }
        }
    }

    public boolean isBookInCart(int bookId) {
        for (Book book : cartItems) {
            if (book.getId() == bookId) {
                return true;
            }
        }
        return false;
    }

    public List<Book> getCartItems() {
        return new ArrayList<>(cartItems);
    }

    public int getCartItemCount() {
        return cartItems.size();
    }

    public void clearCart() {
        cartItems.clear();
        saveCartItems();
    }

    private void loadCartItems() {
        String json = prefs.getString(CART_ITEMS_KEY, "");
        if (json.isEmpty()) {
            cartItems = new ArrayList<>();
        } else {
            Type type = new TypeToken<List<Book>>() {}.getType();
            cartItems = gson.fromJson(json, type);
            if (cartItems == null) {
                cartItems = new ArrayList<>();
            }
        }
    }

    private void saveCartItems() {
        String json = gson.toJson(cartItems);
        prefs.edit().putString(CART_ITEMS_KEY, json).apply();
    }
}