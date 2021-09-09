/*
 * SecCrest Technology GmbH 2021. All rights reserved.
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *  
 * For details to the License read https://www.seccrest-technology.com/development/license
 */
package com.sct.scts.data.commands;

/**
 * 
 * @author Benjamin.Schiller
 */
public class OpenPositionCommand extends AbstractCommand
{

	private int amount;
	private long timestamp;
	private String instrumentId;

	public OpenPositionCommand()
	{
	}

	public OpenPositionCommand(String instrumentId, int amount, long timestamp)
	{
		assert instrumentId != null;

		this.instrumentId = instrumentId;
		this.amount = amount;
		this.timestamp = timestamp;
	}

	public int getAmount()
	{
		return amount;
	}

	public void setAmount(int amount)
	{
		this.amount = amount;
	}

	public String getInstrumentId()
	{
		return instrumentId;
	}

	public void setInstrumentId(String instrumentId)
	{
		assert instrumentId != null;
		
		this.instrumentId = instrumentId;
	}

	public long getTimestamp()
	{
		return timestamp;
	}

	public void setTimestamp(long timestamp)
	{
		this.timestamp = timestamp;
	}
	
	//... read and write data
}
