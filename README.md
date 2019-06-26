# stream-lab

## Users

| Method        | Path                              | Description                   | Status        |
|:------------- |:--------------------------------- |:----------------------------- |:------------- |
| `GET`         | /users                            | get all users                 | Done          |
| `GET`         | /users/{user-id}                  | get user                      | Done          |
| `POST`        | /users                            | new users                     | Done          |
| `DELETE`      | /users/{user-id}                  | delete user                   | Done          |
| `GET`         | /users/subs                       | get all subs users            | Done          |
| `PATCH`       | /users/subs                       | new subs                      | Done          |
| `PATCH`       | /users/unsubs                     | cancel subs                   | Done          |
| `GET`         | /users/blacklist                  | get all blacklisted users     | Done          |
| `PATCH`       | /users/blacklist                  | blacklisted user              | Done          |
| `PATCH`       | /users/unblacklist                | remover user from blacklisted | Done          |

## Tips

| Method        | Path                  | Description                  | Status        | 
|:------------- |:----------------------|:---------------------------- |:------------- |
| `GET`         | /tips                 | get all tips                 | Done          |
| `POST`        | /tips                 | new tips                     | Done          |
| `DELETE`      | /tips/{tips-id}       | cancel tips                  | Done          |
| `GET`         | /tips/sum             | sum all tips                 | Done          |
| `GET`         | /tips/users           | get all the donators         | Done          |
| `GET`         | /tips/users/{id}/sum  | tips sum of a user           | Done          |
| `GET`         | /tips/users/sum       | tips sum group by user       | In progress   |

## Giveaways

| Method        | Path                          | Description                  | Status        |
|:------------- |:----------------------------- |:---------------------------- |:------------- |
| `GET`         | /giveaways                    | get all giveaways            | Done          |
| `GET`         | /giveaways/{giveaway-id}      | get giveaways                | Done          |
| `POST`        | /giveaways                    | new giveaway                 | Done          |
| `POST`        | /giveaways/participate        | participate to a giveaway    | Done          |
| `DELETE`      | /giveaways/{giveaways-id}     | delete giveaway              | Done          |
| `GET`         | /giveaways/{giveaway-id}/draw | draw a giveaway              | Done          |

## Polls

| Method        | Path                        | Description             | Status        |
|:------------- |:--------------------------- |:----------------------- |:------------- |
| `GET`         | /polls                      | get all polls           | Done          |
| `POST`        | /polls                      | new polls               | Done          |
| `PATCH`       | /polls/participate          | participate to a polls  | Done          |
| `GET`         | /polls/{poll-id}/result     | get poll result         | Done          |