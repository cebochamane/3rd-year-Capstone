public class Seat {

    private Position position;

    /**
     * Creates a seat at the specified position.
     *
     * @param position the position of the seat at the table
     */
    public Seat(Position position) {
        this.position = position;
    }

    /**
     * Returns the position of this seat.
     *
     * @return the seat's position
     */
    public Position getPosition() {
        return this.position;
    }

    /**
     * Returns the seat immediately clockwise from this seat.
     *
     * The order is NORTH -> EAST -> SOUTH -> WEST -> NORTH.
     *
     * @return the next seat
     */
    public Seat next() {
        // TODO: implement
        return null;
    }

    /**
     * Returns the seat immediately counter-clockwise from this seat.
     *
     * The order is NORTH -> WEST -> SOUTH -> EAST -> NORTH.
     *
     * @return the previous seat
     */
    public Seat previous() {
        // TODO: implement
        return null;
    }

    /**
     * Returns the seat directly opposite this seat.
     *
     * For example, North is opposite South and East is
     * opposite West.
     *
     * @return the opposite seat
     */
    public Seat opposite() {
        // TODO: implement
        return null;
    }

    /**
     * Returns a human-readable representation of the seat.
     *
     * @return the name of the seat's position
     */
    @Override
    public String toString() {
        // TODO: implement
        return "";
    }
}