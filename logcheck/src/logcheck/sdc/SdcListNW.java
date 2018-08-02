package logcheck.sdc;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import logcheck.annotations.UseSdcListNW;
import logcheck.annotations.WithElaps;
import logcheck.util.ClientAddr;
import logcheck.util.NetAddr;

/*
 * Checker50のsdclistの対象をNetworkアドレスに限定した.
 */
@UseSdcListNW
public class SdcListNW extends SdcList {

	private static final long serialVersionUID = 1L;

	@Override
	@WithElaps
	public SdcList load(String file) throws IOException {
		SdcList isps = new SdcList();
		try (Stream<String> input = Files.lines(Paths.get(file), Charset.forName("MS932"))) {
			input.filter(SdcList::test)
				.map(SdcList::parse)
				.forEach(b -> {
					final SdcListIsp isp = new SdcListIsp(b.getName(), b.getType());

					if (!b.getAddr().contains("/")) {
						isp.addAddress(new ClientAddr(b.getAddr()));
						this.add(isp);
					}
					else {
						isp.addAddress(new NetAddr(b.getAddr()));
						isps.add(isp);
					}
				});
		}
		// hostが所属するnetworkが存在する場合はNW名を設定する
		// なぜならば、hostのnameにはホスト名が設定されているため
		this.stream()
			.forEach(host -> 
				host.getAddress().stream()
					.forEach(addr -> {
						SdcListIsp isp = isps.get(addr);
						if (isp == null) {
							Logger.getLogger(SdcListNW.class.getName()).log(Level.WARNING, "(SdcList): not foun isp (isp={0})", isp);
						}
						else {
							host.setName(isp.getName());
						}
					})
			);
		isps.stream().forEach(this::add);
		return this;
	}

}
