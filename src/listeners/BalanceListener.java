package listeners;

import models.User;

public interface BalanceListener {
    void onBalanceChanged(User user);
}