/*
 * Copyright 2018 Alexander Donofero
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.adonofero.challenge.entity;

import java.util.Objects;

/**
 * POJO for a User
 *
 * @author Alexander Donofero
 */
public class User {
    private String name;
    private int uid;
    private int gid;
    private String comment;
    private String home;
    private String shell;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public int getGid() {
        return gid;
    }

    public void setGid(int gid) {
        this.gid = gid;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getHome() {
        return home;
    }

    public void setHome(String home) {
        this.home = home;
    }

    public String getShell() {
        return shell;
    }

    public void setShell(String shell) {
        this.shell = shell;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return uid == user.uid &&
                gid == user.gid &&
                Objects.equals(name, user.name) &&
                Objects.equals(comment, user.comment) &&
                Objects.equals(home, user.home) &&
                Objects.equals(shell, user.shell);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, uid, gid, comment, home, shell);
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", uid=" + uid +
                ", gid=" + gid +
                ", comment='" + comment + '\'' +
                ", home='" + home + '\'' +
                ", shell='" + shell + '\'' +
                '}';
    }
}
