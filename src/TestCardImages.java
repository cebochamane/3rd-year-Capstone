import java.awt.Image;

public class TestCardImages {
    public static void main(String[] args) {
        CardImageLoader.loadCardImages();
        System.out.println("Images loaded: " + CardImageLoader.areImagesLoaded());

        Image card = CardImageLoader.getCardImage(Suit.SPADES, Rank.ACE);
        System.out.println("Ace of Spades loaded: " + (card != null));

        Image back = CardImageLoader.getCardBack();
        System.out.println("Card back loaded: " + (back != null));
    }
}