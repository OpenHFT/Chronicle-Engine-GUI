package software.chronicle;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ConvertExtractToPrompts {

    public static void main(String[] args) throws IOException {
        for (int i = 4; i < 28; i++) {
            extracted("/Users/robaustin/git-projects/gpt3/src/main/resources/extracted/"+i, "/Users/robaustin/git" +
                    "-projects" +
                    "/gpt3/src/main/resources/prompts/"+i);
        }

    }

    private static void extracted(String sourceFilePath, String targetFilePath) throws IOException {
        String content = new String(Files.readAllBytes(Paths.get(sourceFilePath)), StandardCharsets.UTF_8);
        StringBuilder result = new Gpt().callGpt(content, "convert what you are given into as prompts and answers as " +
                "possible that can be " +
                "used for fine tuning, please only provide the prompts and the answers don't add anything else such " +
                "as a title like 'Possible prompts and answers for fine tuning:', don't provided answers like 'an be " +
                "accessed using the following dependency' with out including these dependencies or examples in your " +
                "answer" +
                ", provide your answer in JSON format \"prompt\":\"<prompt>\", \"answer\":\"<answer>\"\n  ");
        System.out.println(result);


        ReadRawData.tofile( result.toString(), targetFilePath);
    }

}
