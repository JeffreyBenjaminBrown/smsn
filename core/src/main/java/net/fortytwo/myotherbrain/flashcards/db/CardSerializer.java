package net.fortytwo.myotherbrain.flashcards.db;

import net.fortytwo.myotherbrain.flashcards.Card;
import net.fortytwo.myotherbrain.flashcards.Deck;

import java.io.IOException;

/**
 * User: josh
 * Date: 3/29/11
 * Time: 5:00 PM
 */
public interface CardSerializer<Q, A> {
    String serialize(Card<Q, A> card) throws IOException;

    Card<Q, A> deserialize(String name,
                           Deck<Q, A> deck,
                           String data) throws IOException;
}