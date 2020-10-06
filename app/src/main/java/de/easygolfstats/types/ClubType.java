package de.easygolfstats.types;

import de.easygolfstats.types.ClubCategory;

public enum ClubType {

    IRON_1 (0, ClubCategory.IRON),
    IRON_2 (1, ClubCategory.IRON),
    IRON_3 (2, ClubCategory.IRON),
    IRON_4 (3, ClubCategory.IRON),
    IRON_5 (4, ClubCategory.IRON),
    IRON_6 (5, ClubCategory.IRON),
    IRON_7 (6, ClubCategory.IRON),
    IRON_8 (7, ClubCategory.IRON),
    IRON_9 (8, ClubCategory.IRON),
    PITCHING_WEDGE (9, ClubCategory.WEDGE),
    SAND_WEDGE (10, ClubCategory.WEDGE),
    A_WEDGE (11, ClubCategory.WEDGE),
    GAP_WEDGE (12, ClubCategory.WEDGE),
    WEDGE_ANGLE_1 (13, ClubCategory.WEDGE),
    WEDGE_ANGLE_2 (14, ClubCategory.WEDGE),
    WEDGE_ANGLE_3 (15, ClubCategory.WEDGE),
    WEDGE_ANGLE_4 (16, ClubCategory.WEDGE),
    CHIPPER (17, ClubCategory.CHIPPER),
    WOOD_1 (18, ClubCategory.WOOD),
    WOOD_2 (19, ClubCategory.WOOD),
    WOOD_3 (20, ClubCategory.WOOD),
    WOOD_4 (21, ClubCategory.WOOD),
    WOOD_5 (21, ClubCategory.WOOD),
    WOOD_6 (23, ClubCategory.WOOD),
    HYBRID_3 (24, ClubCategory.HYBRID),
    HYBRID_4 (25, ClubCategory.HYBRID),
    HYBRID_5 (26, ClubCategory.HYBRID),
    DRIVER_1 (27, ClubCategory.DRIVER),
    DRIVER_2 (28, ClubCategory.DRIVER),
    PUTTER_1 (29, ClubCategory.PUTTER),
    PUTTER_2 (30, ClubCategory.PUTTER);

    private int typeIndex;
    private ClubCategory clubCategory;

    ClubType (int typeIndex, ClubCategory clubCategory) {
        this.typeIndex = typeIndex;
        this.clubCategory = clubCategory;
    }

    public int getTypeIndex() {
        return typeIndex;
    }

    public ClubCategory getClubCategory () {
        return clubCategory;
    }
}
