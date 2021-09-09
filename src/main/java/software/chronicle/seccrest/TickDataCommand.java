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
public class TickDataCommand extends AbstractCommand
{

	private long timestamp;
	private double ask;
	private double bid;
	private String instrumentId;

	public TickDataCommand()
	{

	}

	public TickDataCommand(long timestamp, double ask, double bid, String instrumentId)
	{
		assert instrumentId != null;

		this.timestamp = timestamp;
		this.ask = ask;
		this.bid = bid;
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

	public double getAsk()
	{
		return ask;
	}

	public void setAsk(double ask)
	{
		this.ask = ask;
	}

	public double getBid()
	{
		return bid;
	}

	public void setBid(double bid)
	{
		this.bid = bid;
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
	
	// ... read and write data	
}
