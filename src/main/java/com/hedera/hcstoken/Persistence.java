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
import java.util.Objects;

/**
 * This class handles the persistence of the token data
 * In this example we persist to a file, but a real implementation would
 * typically use a database for this purpose
 */
public final class Persistence {
    private static final String fileName = AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_ID"))).toString() + ".json";
    /**
     * Loads token data from a file
     * @throws Exception: in the event of an error
     */
    public static Token loadToken() throws Exception {
        Token token = new Token();
        File stateFile = new File(fileName);
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
        ObjectMapper objectMapper = new ObjectMapper();
        File stateFile = new File(fileName);
        objectMapper.writeValue(stateFile, token);
    }
}
