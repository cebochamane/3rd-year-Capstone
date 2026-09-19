// Thin adapter only: all double-dummy searching is performed by upstream DDS.
#include "vendor/dds-2.9.0/include/dll.h"
#include <iostream>
#include <string>

int main() {
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
