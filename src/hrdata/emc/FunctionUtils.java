/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hrdata.emc;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author o.nekriach
 */
public class FunctionUtils {

    /**
     * Return team name by name from JSON.
     *
     * @param obj - JSON object stores teams. Output of getTeams function
     */
    public String getNameByIDFromJSON(JSONObject obj, String id) {
        JSONArray array = obj.getJSONObject(Constant.RESULT).getJSONArray(Constant.TEAMS);
        Iterator it = array.iterator();
        while (it.hasNext()) {
            JSONObject jsonTeam = new JSONObject(it.next());
            String teamId = jsonTeam.getString(Constant.ID);
            if (teamId.equals(id)) {
                return jsonTeam.getString(Constant.NAME);
            }
        }
        return null;
    }

    /**
     * Put team id and name as HashMap to Cache from JSON.
     *
     * @param obj - JSON object stores teams. Output of getTeams function
     */
    static public void putTeamsNamesToCache(JSONObject obj) {
        Map<String, String> teamsNamesArr = new HashMap<String, String>();
        JSONArray array = obj.getJSONObject(Constant.RESULT).getJSONArray(Constant.TEAMS);
        Iterator it = array.iterator();
        while (it.hasNext()) {
            JSONObject jsonTeam = new JSONObject(it.next().toString());
            String teamId = jsonTeam.getString(Constant.ID);
            String teamName = jsonTeam.getString(Constant.NAME);
            teamsNamesArr.put(teamId, teamName);
        }
        Cache.setTeamsNamesCache(teamsNamesArr);
    }
}
