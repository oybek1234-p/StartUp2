package com.org.market

import com.org.net.models.User
import com.org.net.models.UserFull

object DataCenter {
    private val users = HashMap<String,User>()
    private val userFulls = HashMap<String,UserFull>()

    fun putUser(user: User): Boolean {
        user.apply {
            users[id].let { oldUser->
                if (oldUser == user) {
                    return false
                }
            }
            users[id] = user

            UserConfig.apply {
                if (id == user().id) {
                    userFull.user = user
                    saveConfig()
                }
            }
        }
        return true
    }

    fun putFullUser(fullUser: UserFull,fromServer: Boolean = false): Boolean {
        fullUser.apply {
            userFulls[user.id] = this

            if (fromServer) {
                UserConfig.apply {
                    if (user.id == user().id) {
                        userFull = fullUser
                        saveConfig()
                        return true
                    }
                }
            }
        }
        return false
    }
}