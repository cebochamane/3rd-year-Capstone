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
        switch (position) {
            case NORTH:
                return new Seat(Position.EAST);
            case EAST:
                return new Seat(Position.SOUTH);
            case SOUTH:
                return new Seat(Position.WEST);
            case WEST:
                return new Seat(Position.NORTH);
        }
        throw new IllegalStateException("Unkown position:" + position);
    }

    /**
     * Returns the seat immediately counter-clockwise from this seat.
     *
     * The order is NORTH -> WEST -> SOUTH -> EAST -> NORTH.
     *
     * @return the previous seat
     */
    public Seat previous() {
        switch (position) {
            case NORTH:
                return new Seat(Position.WEST);
            case EAST:
                return new Seat(Position.NORTH);
            case SOUTH:
                return new Seat(Position.EAST);
            case WEST:
                return new Seat(Position.SOUTH);
        }
        throw new IllegalStateException("Unknown position:" + position);
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
        switch (position) {
            case NORTH:
                return new Seat(Position.SOUTH);
            case EAST:
                return new Seat(Position.WEST);
            case SOUTH:
                return new Seat(Position.NORTH);
            case WEST:
                return new Seat(Position.EAST);
        }
        throw new IllegalStateException("Unknown position:" + position);
    }

    /**
     * Returns a human-readable representation of the seat.
     *
     * @return the name of the seat's position
     */
    @Override
    public String toString() {
        return position.toString();
    }

    /**
     * Indicates whether some other Seat is "equal to" this one.
     *
     * @param otherObject The object to compare this seat with
     * @return true if the other object is an instance of Seat and has the same position, false otherwise
     */
    @Override
    public boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (!(otherObject instanceof Seat)) {
            return false;
        }

        Seat otherSeat = (Seat) otherObject;
        return this.position == otherSeat.position;
    }

    /**
     * Returns a hash code value for the Seat based on the position attribute.
     *
     * @return The hash code value for this object
     */
    @Override
    public int hashCode() {
        return position == null ? 0 : position.hashCode();
    }

    /**
     * Checks whether this Seat is the partner of another seat.
     * 
     * @param other the seat to compare with
     * @return true if the seats are partners, otherwise false
     */
    public boolean isPartner(Seat other){
        if(other == null){
            return false;
        }
        return this.position == other.opposite().getPosition();
    }
}