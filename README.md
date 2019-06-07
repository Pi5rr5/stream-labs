# stream-lab

## Users

| Method        | Path                              | Description                   |
|:------------- |:--------------------------------- |:----------------------------- |
| `GET`         | /users                            | get all users                 |
| `GET`         | /users/{user-id}                  | get user                      |
| `POST`        | /users                            | new users                     |
| `DELETE`      | /users/{user-id}                  | cancel users                  |
| `GET`         | /users?filter="sub"               | get all subs users            |
| `POST`        | /users/subs                       | new subs                      |
| `POST`        | /users/unsubs/{user-id}           | cancel subs                   |
| `GET`         | /users/{user-id}/tips             | get all user's tips           |
| `GET`         | /users?groupby="user"&aggs="sum"  | tips sum group by user        |
| `GET`         | /users/blacklist                  | get all blacklisted users     |
| `POST`        | /users/blacklist                  | blacklisted user              |
| `POST`        | /users/unblacklist/{user-id}      | remover user from blacklisted |

## Tips

| Method        | Path               | Description                  |
|:------------- |:------------------ |:---------------------------- |
| `GET`         | /tips              | get all tips                 |
| `POST`        | /tips              | new tips                     |
| `DELETE`      | /tips/{tips-id}    | cancel tips                  |
| `GET`         | /tips?aggs="sum"   | sum all tips                 |

## Giveaways

| Method        | Path                          | Description                  |
|:------------- |:----------------------------- |:---------------------------- |
| `GET`         | /giveaways                    | get all giveaways            |
| `GET`         | /giveaways/{giveaway-id}      | get giveaways                |
| `POST`        | /giveaways                    | new giveaway                 |
| `POST`        | /giveaways/participate        | participate to a giveaway    |
| `GET`         | /giveaways/{giveaway-id}/draw | draw a giveaway              |

## Surveys

| Method        | Path                        | Description             |
|:------------- |:--------------------------- |:----------------------- |
| `GET`         | /surveys                    | get all surveys         |
| `POST`        | /surveys                    | new survey              |
| `POST`        | /surveys/participate        | participate to a survey |
| `GET`         | /surveys/{survey-id}/result | get survey's result     |