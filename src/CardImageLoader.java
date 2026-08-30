import javax.imageio.ImageIO;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class CardImageLoader {

    private static Map<String, BufferedImage> cardImages = new HashMap<>();
    private static boolean imagesLoaded = false;

    /**
     * Loads all card images from the resources folder.
     * Card images should be in /cards/ directory.
     * Filename format: rank_of_suit.png (e.g., ace_of_spades.png)
     */
    public static void loadCardImages() {
        if (imagesLoaded)
            return;

        try {
            // Load all 52 cards
            for (Suit suit : Suit.values()) {
                if (suit == Suit.NT)
                    continue;
                for (Rank rank : Rank.values()) {
                    String fileName = getFileName(suit, rank);
                    String path = "/cards/" + fileName;

                    URL resource = CardImageLoader.class.getResource(path);
                    if (resource != null) {
                        BufferedImage image = ImageIO.read(resource);
                        if (image != null) {
                            cardImages.put(suit.name() + "_" + rank.name(), image);
                        }
                    } else {
                        System.out.println("Warning: Could not find " + path);
                    }
                }
            }

            // Load card back
            URL backResource = CardImageLoader.class.getResource("/cards/back.png");
            if (backResource != null) {
                BufferedImage backImage = ImageIO.read(backResource);
                if (backImage != null) {
                    cardImages.put("BACK", backImage);
                }
            }

            imagesLoaded = true;
            System.out.println("Loaded " + cardImages.size() + " card images");

        } catch (IOException e) {
            System.out.println("Error loading card images: " + e.getMessage());
            imagesLoaded = false;
        }
    }

    private static String getFileName(Suit suit, Rank rank) {
        // Convert rank to lowercase with proper names
        String rankName = getRankFileName(rank);
        String suitName = getSuitFileName(suit);
        return rankName + "_of_" + suitName + ".png";
    }

    private static String getRankFileName(Rank rank) {
        switch (rank) {
            case TWO:
                return "2";
            case THREE:
                return "3";
            case FOUR:
                return "4";
            case FIVE:
                return "5";
            case SIX:
                return "6";
            case SEVEN:
                return "7";
            case EIGHT:
                return "8";
            case NINE:
                return "9";
            case TEN:
                return "10";
            case JACK:
                return "jack";
            case QUEEN:
                return "queen";
            case KING:
                return "king";
            case ACE:
                return "ace";
            default:
                return "unknown";
        }
    }

    private static String getSuitFileName(Suit suit) {
        switch (suit) {
            case CLUBS:
                return "clubs";
            case DIAMONDS:
                return "diamonds";
            case HEARTS:
                return "hearts";
            case SPADES:
                return "spades";
            default:
                return "unknown";
        }
    }

    public static Image getCardImage(Suit suit, Rank rank) {
        if (!imagesLoaded) {
            loadCardImages();
        }
        String key = suit.name() + "_" + rank.name();
        return cardImages.get(key);
    }

    public static Image getCardBack() {
        if (!imagesLoaded) {
            loadCardImages();
        }
        return cardImages.get("BACK");
    }

    public static boolean areImagesLoaded() {
        return imagesLoaded;
    }

    /**
     * Scales an image to the desired size.
     */
    public static Image getScaledCardImage(Suit suit, Rank rank, int width, int height) {
        Image image = getCardImage(suit, rank);
        if (image == null)
            return null;
        return image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
    }

    public static Image getScaledCardBack(int width, int height) {
        Image image = getCardBack();
        if (image == null)
            return null;
        return image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
    }
}