/*
 * Copyright (c) 2020 FabricMC
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

package io.github.coolcrabs.accesswidener;

/**
 * Thrown when an access widener file couldn't be read due to an incorrect format.
 */
public class AccessWidenerFormatException extends RuntimeException {
    private final int lineNumber;

    public AccessWidenerFormatException(int lineNumber, String message) {
        super(message);
        this.lineNumber = lineNumber;
    }

    /**
     * The line on which the error occurred. Starts with 1.
     */
    public int getLineNumber() {
        return lineNumber;
    }

    @Override
    public String toString() {
        return getClass().getName() + ": line " + lineNumber + ": " + getMessage();
    }
}
