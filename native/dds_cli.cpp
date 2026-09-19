// Thin adapter only: all double-dummy searching is performed by upstream DDS.
#include "vendor/dds-2.9.0/include/dll.h"
#include <iostream>
#include <string>

int main(int argc, char **argv) {
    if (argc == 2 && std::string(argv[1]) == "--table") {
        ddTableDeal tableDeal{};
        for (int hand = 0; hand < 4; ++hand) {
            for (int suit = 0; suit < 4; ++suit) {
                if (!(std::cin >> tableDeal.cards[hand][suit])) return 2;
            }
        }
        std::string extra;
        if (std::cin >> extra) return 2;
        SetResources(96, 1);
        ddTableResults result{};
        int status = CalcDDtable(tableDeal, &result);
        if (status != RETURN_NO_FAULT) {
            char message[80]{};
            ErrorMessage(status, message);
            std::cerr << "DDS table error " << status << ": " << message << "\n";
            FreeMemory();
            return 3;
        }
        // DDS ordering: S/H/D/C/NT rows, N/E/S/W declarer columns.
        std::cout << "DDS20900_TABLE";
        for (int strain = 0; strain < 5; ++strain) {
            for (int declarer = 0; declarer < 4; ++declarer) {
                std::cout << " " << result.resTable[strain][declarer];
            }
        }
        std::cout << "\n";
        FreeMemory();
        return 0;
    }
    if (argc != 1) return 2;
    deal position{};
    int count;
    if (!(std::cin >> position.trump >> position.first >> count)
            || count < 0 || count > 3) {
        std::cerr << "Invalid position header\n";
        return 2;
    }
    for (int i = 0; i < count; ++i) {
        if (!(std::cin >> position.currentTrickSuit[i] >> position.currentTrickRank[i])) return 2;
    }
    for (int hand = 0; hand < 4; ++hand) {
        for (int suit = 0; suit < 4; ++suit) {
            if (!(std::cin >> position.remainCards[hand][suit])) return 2;
        }
    }
    std::string extra;
    if (std::cin >> extra) return 2;
    SetResources(96, 1);
    futureTricks result{};
    // target=-1: maximum tricks; solutions=1; mode=1: calculate the score
    // even when there is only one legal card. Result is for the side to play.
    int status = SolveBoard(position, -1, 1, 1, &result, 0);
    if (status != RETURN_NO_FAULT) {
        char message[80]{};
        ErrorMessage(status, message);
        std::cerr << "DDS error " << status << ": " << message << "\n";
        FreeMemory();
        return 3;
    }
    if (result.cards < 1 || result.score[0] < 0 || result.score[0] > 13) {
        std::cerr << "DDS returned an invalid result\n";
        FreeMemory();
        return 4;
    }
    std::cout << "DDS20900 " << result.score[0] << "\n";
    FreeMemory();
    return 0;
}
