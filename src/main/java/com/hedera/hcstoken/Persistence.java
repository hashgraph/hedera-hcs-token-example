package com.hedera.hcstoken;

/*-
 * ‌
 * hcs-token-example
 * ​
 * Copyright (C) 2020 Hedera Hashgraph, LLC
 * ​
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ‍
 */

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hcstoken.state.Token;
import io.github.cdimascio.dotenv.Dotenv;

import java.io.File;

/**
 * This class handles the persistence of the token data
 * In this example we persist to a file, but a real implementation would
 * typically use a database for this purpose
 */
public final class Persistence {
    /**
     * Loads token data from a file
     * @throws Exception: in the event of an error
     */
    public static Token loadToken() throws Exception {
        final String fileName = AccountId.fromString(Dotenv.configure().ignoreIfMissing().load().get("OPERATOR_ID")).toString() + ".json";
        Token token = new Token();
        File stateFile = new File(fileName);
        return loadToken(stateFile);
    }
    /**
     * Loads token data from a file
     * This is for unit testing purposes, should call loadToken() otherwise
     * @throws Exception: in the event of an error
     */
    public static Token loadToken(File stateFile) throws Exception {
        Token token = new Token();
        if (stateFile.exists()) {
            // a file containing existing state exists, let's load it
            ObjectMapper objectMapper = new ObjectMapper();
            token = objectMapper.readValue(stateFile, Token.class);
        }
        return token;
    }
    /**
     * Saves token data to a file
     * @throws Exception: in the event of an error
     */
    public static void saveToken(Token token) throws Exception {
        final String fileName = AccountId.fromString(Dotenv.configure().ignoreIfMissing().load().get("OPERATOR_ID")).toString() + ".json";
        ObjectMapper objectMapper = new ObjectMapper();
        File stateFile = new File(fileName);
        saveToken(token, stateFile);
    }
    /**
     * Saves token data to a file
     * This is for unit testing purposes, should call saveToken() otherwise
     * @throws Exception: in the event of an error
     */
    public static void saveToken(Token token, File stateFile) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.writeValue(stateFile, token);
    }
}
