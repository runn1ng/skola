use warnings;
use strict;

my $wantword = $ARGV[0];

my %all;
for my $result_filename (<results/*.result.*>) {
    my ($way, $word) = $result_filename =~ m{results/(.*)\.result\.(.*)};
    use File::Slurp;
    my $res = read_file("$result_filename");
    chomp $res;
    my ($size, $error) = $res =~ /^(\S+)\s+(\S+)$/;
     
    if ($word eq $wantword){
        #print $way."\t".$word."\t".$size."\t".$error."\t".($size +
         #   $error)."\t".($size - $error)."\n" 
        
        my ($model, $par) = split (/\./, $way);
        my $low_int = $size - $error;
        my $big_int = $size + $error;
        my ($model, $par) = split (/\./, $way);
        $all{ $model."  & ".$par." & ".$res." & $low_int & $high_int ".'\\\\ \hline'} = $res;
  
    }
}

for (sort {$all{$b} <=> $all{$a}} keys %all) {
    print $_."\n";
}
