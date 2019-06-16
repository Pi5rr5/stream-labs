# stream-lab

## Users

| Method        | Path                              | Description                   | Status        |
|:------------- |:--------------------------------- |:----------------------------- |:------------- |
| `GET`         | /users                            | get all users                 | Done          |
| `GET`         | /users/{user-id}                  | get user                      | Done          |
| `POST`        | /users                            | new users                     | Done          |
| `DELETE`      | /users/{user-id}                  | cancel users                  | Done          |
| `GET`         | /users?filter="sub"               | get all subs users            | In progess    |
| `PATCH`       | /users/subs                       | new subs                      | In progess    |
| `PATCH`       | /users/unsubs/{user-id}           | cancel subs                   | In progess    |
| `GET`         | /users/{user-id}/tips             | get all user's tips           | In progess    |
| `GET`         | /users?groupby="user"&aggs="sum"  | tips sum group by user        | In progess    |
| `GET`         | /users/blacklist                  | get all blacklisted users     | Done          |
| `PATCH`       | /users/blacklist                  | blacklisted user              | Done          |
| `PATCH`       | /users/unblacklist/{user-id}      | remover user from blacklisted | In progess    |

## Tips

| Method        | Path               | Description                  | Status        | Status        |
|:------------- |:------------------ |:---------------------------- |:------------- |:------------- |
| `GET`         | /tips              | get all tips                 | In progess    | In progess    |
| `POST`        | /tips              | new tips                     | In progess    | In progess    |
| `DELETE`      | /tips/{tips-id}    | cancel tips                  | In progess    | In progess    |
| `GET`         | /tips?aggs="sum"   | sum all tips                 | In progess    | In progess    |

## Giveaways

| Method        | Path                          | Description                  | Status        |
|:------------- |:----------------------------- |:---------------------------- |:------------- |
| `GET`         | /giveaways                    | get all giveaways            | In progess    |
| `GET`         | /giveaways/{giveaway-id}      | get giveaways                | In progess    |
| `POST`        | /giveaways                    | new giveaway                 | In progess    |
| `POST`        | /giveaways/participate        | participate to a giveaway    | In progess    |
| `GET`         | /giveaways/{giveaway-id}/draw | draw a giveaway              | In progess    |

## Surveys

| Method        | Path                        | Description             | Status        |
|:------------- |:--------------------------- |:----------------------- |:------------- |
| `GET`         | /surveys                    | get all surveys         | In progess    |
| `POST`        | /surveys                    | new survey              | In progess    |
| `POST`        | /surveys/participate        | participate to a survey | In progess    |
| `GET`         | /surveys/{survey-id}/result | get survey's result     | In progess    |