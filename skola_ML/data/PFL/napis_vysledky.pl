use warnings;
use strict;

my $wantword = $ARGV[0];

for my $result_filename (<results/*.result.*>) {
    my ($way, $word) = $result_filename =~ m{results/(.*)\.result\.(.*)};
    use File::Slurp;
    my $res = read_file("$result_filename");
    chomp $res;
    my ($size, $error) = $res =~ /^(.*)\s*(.*)$/;
    print $way."\t".$word."\t".$size."\t".$error."\n" if ($word eq $wantword);
}
